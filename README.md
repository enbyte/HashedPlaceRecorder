# HashedPlaceRecorder

Extremely simple Spigot plugin that stops the building of swastikas while also preventing the server owner from spying on everyone's builds trivially.

## How it works:

1) When someone places a block, the plugin gets their username as well as the x, y, and z.
2) It hashes the x,y,z together 50 times
3) It writes the line username,hash; to the `hashed.locations` file
4) That's it

### What is a hash function?
If the plugin just wrote the place coordinates to the file, it would be extremely trivial for Ryan to just open the file and clearly see where anyone's base is.
A hash function is a function that can only be done one way on some data: It takes some input and returns a fixed-length output that's different for every input. It's very, very easy to go from input to output,
but given the output it's virtually impossible to figure out what the input was. 

If you can figure out a way to consistently figure out an input from that output, congratulations, modern internet security is dead. Good luck!

The same input always produces the same output, but very small changes in the input produce very big changes in the output. This is to avoid being able to find patterns and reverse what the input was. For example, using the hash function that this plugin uses (called SHA-256):
The hash of `hello` is `2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824`.
But if we change even one letter: the hash of `hfllo` is `b32e7c6974ea637e06db9f8861c43332be7c8a49e6e703b12733a568febf1617`. 


### Ok, if we can't get the input back, how is this useful at all?
Because the hash function always produces the same output from the same input, if we see at 1234, -12, -12345 someone has built a swastika then we can just take those coords, 
format them in the manner `x,y,z` (so `1234,-12,-12345`), hash that 50 times, and then search in the place locations for that specific hash and see the attached username.

But, you can't go from the hash to the coords, so all ryan knows for any arbitrary log entry is "username placed a block *somewhere*". If he wanted to brute-force every possible place location hash for -20k to 20k,
then he would have to compute: 40,000 (possible x-coords) * 40,000 (possible z-coords) * 256 (realistic possible y-coords) * 50 (hashes done per location) = 20,480,000,000,000 (20 trillion hashes). 
Put simply, this will take a little while. And that's just -20k to 20k overworld!


