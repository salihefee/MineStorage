package tr.com.salihefee.minestorage;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

import static tr.com.salihefee.minestorage.Maps.*;

public final class MineStorage extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Hello, World!");

        Objects.requireNonNull(getCommand("placeblock")).setExecutor(this);

        getServer().getPluginManager().registerEvents(this, this);
        
        createLookupTable();
        createByteLookupTable();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Bye, World :(");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(event.getPlayer().getDisplayName() + " joined the server.");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        World world = getServer().getWorld("world");

        assert world != null;

        Player player;

        int size = 16;
        
        try {
            player = (Player) sender;
        } catch (CommandException e) {
            getLogger().info("This command is made for use in-game.");
            return false;
        }

        Location playerLoc = player.getLocation();
        
        if (label.equalsIgnoreCase("placeblock")) {

            File input = new File("/home/salihefee/Documents/MineStorageInput/" + args[0]);
            byte[] fileContent = fileToByteArray(input, this);
            
            int startX = (int) Math.floor(playerLoc.getX()) - 20;
            int startY = (int) Math.floor(playerLoc.getY()) - 1;
            int startZ = (int) Math.floor(playerLoc.getZ());
            
            int cursor = 0;

                boolean run = true;
    
                byte currentByte;
                while (run) {
                    for (int offsetZ = 0; run && offsetZ < size * size; offsetZ++) {
                        for (int offsetY = size; run && offsetY > 0; offsetY--) {
                            for (int offsetX = size; offsetX > 0; offsetX--) {
                                try {
                                    currentByte = fileContent[cursor];
                                }
                                catch (IndexOutOfBoundsException e) {
                                    run = false;
                                    break;
                                }
                                world.getBlockAt(startX + offsetX, startY + offsetY, startZ + offsetZ).setType(blockLookupTable.get(currentByte));
                                cursor++;
                            }   
                        }
                    }
                    startX -= size;
                }
            //noinspection ConstantConditions

            return true;
        }
        if (label.equalsIgnoreCase("readblock")) {
            int startX = (int) Math.floor(playerLoc.getX());
            int startY = (int) Math.floor(playerLoc.getY()) - 1;
            int startZ = (int) Math.floor(playerLoc.getZ());
            
            boolean run = true;
            
            ArrayList<Byte> byteArrayList = new ArrayList<>();

            //noinspection ConstantConditions
            while (run) {
                //noinspection ConstantConditions
                for (int offsetZ = 0; run && offsetZ < size * size; offsetZ++) {
                    //noinspection ConstantConditions
                    for (int offsetY = 0; run && offsetY < size; offsetY++) {
                        for (int offsetX = 0; offsetX < size; offsetX++) {
                            Block block = world.getBlockAt(startX - offsetX, startY - offsetY, startZ + offsetZ);
                            if (block.isEmpty()) {
                                run = false;
                                break;
                            }
                            byteArrayList.add(byteLookupTable.get(block.getType()));
                        }
                    }
                }
                startX -= size;
            }
            
            Byte[] tempArray = new Byte[byteArrayList.size()];
            
            File outputFile = new File("/home/salihefee/Documents/MineStorageOutput/" + args[0]);

            try {
                boolean created = outputFile.createNewFile();
                
                if (created) {
                    getLogger().info("File succesfully created.");
                }
                else {
                    getLogger().info("File already exists.");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            tempArray = byteArrayList.toArray(tempArray);   

            byte[] bytes = new byte[tempArray.length];

            for (int i = 0; i < tempArray.length; i++) bytes[i] = tempArray[i];
            
            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                outputStream.write(bytes);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }
    
    private static byte @NotNull [] fileToByteArray(File file, Plugin plugin) {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            long fileSize = file.length();
            byte[] fileContent = new byte[(int) fileSize];

            int bytesRead = inputStream.read(fileContent);
            
            plugin.getLogger().info("Read " + bytesRead + " bytes.");
            
            return fileContent;
        }
        catch (IOException e) {
            return new byte[0];
        }
    }
}
