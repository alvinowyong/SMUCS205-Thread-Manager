import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;


/**
 * Main thread that spawns and managed other threads within main method. 
 * To run the program java FoodManager N M K W X Y Z based on requirements.
 * 
 *
 * @param N  The number of Hotdogs to be produced.
 * @param M  The number of Burgers to be produced.
 * 
 * @param K  The amount of buffer space available for storage.
 * which invokes {@link #buffer(K)}.
 * 
 * @param W  The number of Hotdog Producer threads to be spawned.
 * @param X  The number of Burger Producer threads to be spawned.
 * which invokes runnable {@link #hotdMach()} and {@link #burgerMach()}.
 * 
 * @param Y  The number of Hotdog Consumer threads to be spawned.
 * @param Z  The number of Burger Consumer threads to be spawned.
 * which invokes runnable {@link #hotdPack()} and {@link #burgerPack()}.
 */
public class FoodManager {
    static int reqHotd, reqBurg, poolSize, numHotdMach, numBurgMach;
    static volatile int numHotdPack = 0;
    static volatile int numBurgPack = 0;
    static volatile int hotdProd = 0;
    static volatile int burgProd = 0;
    static volatile int hotdPack = 0;
    static volatile int burgPack = 0;
    static volatile boolean hotdWaiting = false;

    /* Simulate work for n_seconds seconds */
    static void gowork(int n_seconds) {
        for (int i = 0; i < n_seconds; i++) {
            long n = 300000000;
            while (n > 0) {
                n--;
            }
        }
    }

    /* Reuseable log function to write to 'log.txt' */
    static void log(String message) {
        try {
            final Path path = Paths.get("log.txt");
            Files.write(path, Arrays.asList(message), StandardCharsets.UTF_8,
                    Files.exists(path) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public static void main(String[] args) {
        try {
            /* Delete old log file if exists */
            final Path path = Paths.get("log.txt");
            boolean result = Files.deleteIfExists(path);
        } catch (IOException e) {
            System.out.println(e);
        }
        /* Parse argument provided on program run */
        reqHotd = Integer.parseInt(args[0]);
        reqBurg = Integer.parseInt(args[1]);
        poolSize = Integer.parseInt(args[2]);
        Buffer buffer = new Buffer(poolSize);
        numHotdMach = Integer.parseInt(args[3]);
        numBurgMach = Integer.parseInt(args[4]);
        numHotdPack = Integer.parseInt(args[5]);
        numBurgPack = Integer.parseInt(args[6]);

        /* Log information into log.txt */
        log("hotdogs: " + reqHotd);
        log("burgers: " + reqBurg);
        log("capacity: " + poolSize);
        log("hotdog maker: " + numHotdMach);
        log("burger maker: " + numBurgMach);
        log("hotdog packers: " + numHotdPack);
        log("burger packers: " + numBurgPack);

        /* Runnable method for Hotdog 🌭 Producer Machines */
        Runnable HotdMach = new Runnable() {
            @Override
            public void run() {
                String thread_name = Thread.currentThread().getName();
                while (hotdProd < reqHotd) {
                    gowork(30);
                    synchronized(buffer) {
                        if (hotdProd == reqHotd) {
                            numHotdMach--;
                            buffer.notifyAll();
                            return ;
                        }
                        Hotdog hotdog = new Hotdog(hotdProd, thread_name);
                        hotdProd++;
                        buffer.put(hotdog);
                        log(thread_name + " puts hotdog id: " + hotdog.id);
                    }
                    gowork(10);
                }
                numHotdMach--;
            }
        };
        /* @param W Generate threads based on program arguments */
        for (int i = 0; i < numHotdMach; i++) {
            new Thread(HotdMach, "hm" + i).start();
        }


        /* Runnable method for Burger 🍔 Producer Machines */
        Runnable BurgMach = new Runnable() {
            @Override
            public void run() {
                String thread_name = Thread.currentThread().getName();
                while (burgProd < reqBurg) {
                    gowork(80);
                    synchronized(buffer) {
                        if (burgProd == reqBurg) {
                            numBurgMach--;
                            buffer.notifyAll();
                            return ;
                        }
                        Burger burger = new Burger(burgProd, thread_name);
                        burgProd++;
                        buffer.put(burger);
                        log(thread_name + " puts burger id: " + burger.id);
                    }
                    gowork(10);
                }
                numBurgMach--;
            }
        };
        /* @param X Generate threads based on program arguments */
        for (int i = 0; i < numBurgMach; i++) {
            new Thread(BurgMach, "bm" + i).start();
        }

        /* Runnable method for Hotdog 🌭 Packers */
        Runnable HotdPack = new Runnable() {
            @Override
            public void run() {
                String thread_name = Thread.currentThread().getName();
                while (hotdPack != hotdProd || hotdPack != reqHotd) {
                    if (buffer.isEmpty()) {
                        continue;
                    }
                    Hotdog hotd_one, hotd_two;
                    synchronized(buffer){
                        while (!(buffer.peek() instanceof Hotdog) || hotdWaiting) {
                            if (hotdPack == hotdProd && hotdPack == reqHotd) {
                                buffer.notifyAll();
                                numHotdPack--;
                                return ;
                            }
                            buffer.notifyAll();
                            try { buffer.wait();} catch (Exception e) {}
                        }
                        hotd_one = (Hotdog) buffer.get();
                        hotdPack += 1;
                        hotdWaiting = true;
                        Thread.currentThread().setPriority(10);
                    }

                    synchronized(buffer){
                        while (!(buffer.peek() instanceof Hotdog)) {
                            buffer.notifyAll();
                            try { buffer.wait();} catch (Exception e) {}
                        }
                        hotd_two = (Hotdog) buffer.get();
                        hotdPack += 1;
                        hotdWaiting = false;
                        Thread.currentThread().setPriority(5);
                    }
                    gowork(20);
                    log(thread_name + " gets hotdogs id: " + hotd_one.id + " from " + hotd_one.hotdMach + " and id: " + hotd_two.id + " from " + hotd_two.hotdMach);
                }
                numHotdPack--;
            }
        };
        /* @param Y Generate threads based on program arguments */
        for (int i = 0; i < numHotdPack; i++) {
            new Thread(HotdPack, "hc" + i).start();
        }
        
        /* Runnable method for Burger 🍔 Packer */
        Runnable BurgPack = new Runnable() {
            @Override
            public void run() {
                String thread_name = Thread.currentThread().getName();
                while (burgPack != burgProd || burgPack != reqBurg) {
                    if (buffer.isEmpty()) {
                        continue;
                    }
                    Burger burger;
                    synchronized(buffer){
                        while (!(buffer.peek() instanceof Burger)) {
                            if (burgPack == burgProd && burgPack == reqBurg) {
                                buffer.notifyAll();
                                numBurgPack--;
                                return ;
                            }
                            buffer.notifyAll();
                            try { buffer.wait();} catch (Exception e) {}
                        }
                        burger = (Burger) buffer.get();
                    }
                    gowork(20);
                    burgPack+= 1;
                    log(thread_name + " gets burger id: " + burger.id + " from " + burger.burgMach);
                }
                numBurgPack--;
            }
        };
        /* @param Z Generate threads based on program arguments */
        for (int i = 0; i < numBurgPack; i++) {
            new Thread(BurgPack, "bc" + i).start();
        }

        /* Check for consumer completion */
        while (numHotdPack > 0 || numBurgPack > 0){
            // System.out.println("numhotdpack" + numHotdPack);
            // System.out.println("numburgpack" + numBurgPack);
        }

        /* Create TreeMap to summarise all activities */
        TreeMap<String, Integer> summary = new TreeMap<String, Integer>();
        try (Scanner fileReader = new Scanner(new File("log.txt"))){
            int linenum = 0;
            while (fileReader.hasNext()){
                /* Skip first seven lines of introductory information */
                if (linenum++ < 7) {
                    fileReader.nextLine();
                    continue;
                } else {
                    /* Tokenize and add machine/packer id to */ 
                    int toAdd;
                    String machine = fileReader.nextLine().split(" ")[0];
                    toAdd = machine.contains("hc") ? 2 : 1;
                    if (summary.containsKey(machine)){
                        summary.put(machine, summary.get(machine) + toAdd);
                    } else {
                        summary.put(machine, toAdd);
                    }
                }
                linenum++;
            }

            /* Logging of all activites provided by TreeMap into log file lexiographically */
            log("Summary:");
            for (Map.Entry<String, Integer> entry : summary.entrySet()){
                String key = entry.getKey();
                /* Check if key is producer or consumer */
                if (key.charAt(1) == 'm') {
                    log(key + " makes " + entry.getValue());
                } else {
                    log(key + " packs " + entry.getValue());
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}