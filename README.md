# SiegeCraft
Minecraft Minigame plugin inspirated to the siege game of brawl stars.

The plugin can be interfaced with any other plugin using the methods of SiegeCraft class.
Plugin events are handled using the SiegeBaseListener, if you want to add some function to be run when a event occur you can extend the SiegeListener class, implement only the methods which you want and register it to the plugin using the method register of the SiegeCraft class
if you don't like the basic listeners you can remove it using the method getListeners() of the SiegeCraft class and remove it from the list (position 0).

The plugin offer a full handle of the game you don't need to do anything else other to build a map for it. Remember that the tower is an armor stand that shoots arrows with no gravity, so place it in a place where it has no blocks in range at same height because tower can destroy only blocks placed by players not map blocks.


# Requirements
To compile this project you need external 2 libraries:
* Spigot: https://getbukkit.org/get/bf44510c50ddefccbaee1379c1f751de
* JavaFX: https://gluonhq.com/products/javafx/ (only javafx.base jar is needed) 

# Javadoc
In doc directory you can find the javadoc of each class with all public methods documented

# TO DO
- Upload release binaries
