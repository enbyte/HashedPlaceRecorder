package com.github.enbyte.HashedPlaceRecorder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public final class HashedPlaceRecorder extends JavaPlugin implements Listener {

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public void onDisable() {
		getLogger().info("onDisable has been invoked!");
	}
	
	private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
	
	private static void writeHashedPlaceLocation(String path, String hash) {
		File file = new File(path);
		BufferedWriter writer = null;
		
		try {
			writer = new BufferedWriter(new FileWriter(file, true));
			writer.write(hash);
			writer.newLine();
			return;
		} catch (IOException exc) {
			System.err.println("Error processing file " + path + ": " + exc.getMessage());
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException exc) { // Extremely common java L why do I have to manage my own memory
					System.err.println("Error closing writer " + path + ": " + exc.getMessage());
				}
			}
		}
	}
	
	private static String doChainedHashing(String toHash, int iterations) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		String currentHash = toHash;
		
		for (int i = 1; i <= iterations; i++) {
			byte[] encodedHash = digest.digest(currentHash.getBytes(StandardCharsets.UTF_8));
			currentHash = bytesToHex(encodedHash);
		}
		
		return currentHash;
	}
	
	@EventHandler(priority = EventPriority.MONITOR) // Just listens, can't modify events
	public void onPlace(BlockPlaceEvent event) throws NoSuchAlgorithmException {
		Block placedBlock = event.getBlock();
		Location placedLocation = placedBlock.getLocation();
		
		Player placedPlayer = event.getPlayer();
		String username = placedPlayer.getName();
		
		StringBuilder builder = new StringBuilder();
		
		builder.append(placedLocation.getBlockX());
		builder.append(",");
		builder.append(placedLocation.getBlockY());
		builder.append(",");
		builder.append(placedLocation.getBlockZ());
		
		final String hash = doChainedHashing(builder.toString(), 50);
		final String loggedEvent = username + "," + hash + ";";
		
		BukkitScheduler scheduler = Bukkit.getScheduler();
		scheduler.runTaskAsynchronously(this, () -> writeHashedPlaceLocation("hashed.locations", loggedEvent));
		
	}
	
}
