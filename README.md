# Voidweaver
This is a Fabric/Quilt mod that allows you to create new void dimensions and teleport to them with ease. It is usable as a client-side (singleplayer), or server-side mod. It is not required to install it on the client if it's installed on the server.

Supports teleporting to any dimension that is generated by any* mod. (not tested except for vanilla dims).

**WARNING** this mod does not check if a given dimension is owned by another mod. It is possible to generate a dimension that would be created by another mod and cause issues. **Use with caution!**

## Requirements
Version `1.1.0` requires `Fantasy v0.5.0+1.20.4` to work. This has been tested on both Fabric and Quilt. You can find this release on [GitHub](https://github.com/NucleoidMC/fantasy/releases/tag/v0.5.0%2B1.20.4).

Version `1.2.0` does not require the `Fantasy v0.5.0+1.20.4` dependency to work. If you have issues using this version, try `1.1.0`!

## Usage
- `/voidweaver test` - Test to see if Voidweaver is working
- `/voidweaver new <namespace> <world_name>` - Create a new dimension, accessible by Minecraft and other mods using `namespace:world_name`.
- `/voidweaver jump <namespace> <world_name>` - Teleport to an already created dimension

## Issues
Please submit all issues by following the link in the Modrinth sidebar

## Contributing
All contributions are welcome! I'm personally very new to Minecraft modding, and this is the very first mod I've ever made.

## Credits
- The people of [NucleoidMC](https://github.com/NucleoidMC) for their library [Fantasy](https://github.com/NucleoidMC/fantasy). Without it, this mod wouldn't have been possible.
- The people of the Quilt Discord for their help and guidance in answering my extremely basic questions.
- The people of the Modrinth Discord for their help!

## Future plans
- Check ownership of dimension(s) at runtime
- Keep a list of dimensions created by Voidweaver and register on launch
    - Currently, jumping to a given dimension either registers the previously created dimension, or creates it.
    - Jumping to a dimension not created using the `new` subcommand, generates the dimension without a bedrock block to stand on.
- Warn the user about generating dimensions registered by other mods
- Subcommand to delete dimensions generated by Voidweaver