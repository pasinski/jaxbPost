package mpasinski.commons;

import mpasinski.jaxb.service.furniture.Cupboard;
import mpasinski.jaxb.service.furniture.Fridge;
import mpasinski.jaxb.service.houses.House;
import mpasinski.jaxb.service.houses.rooms.Kitchen;
import mpasinski.jaxb.service.houses.rooms.LivingRoom;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.util.Calendar;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: mpasinski
 * Date: 04.12.12
 * Time: 21:31
 * To change this template use File | Settings | File Templates.
 */
public class TestUtils {
    public static String readClasspathFile(String location) throws IOException {
        Resource resource = new ClassPathResource(location);
        File file = new File(resource.getURI());
        return readFileToString(file);
    }

    public static InputStream readClasspathFileToInputStream(String location) throws IOException {
        Resource resource = new ClassPathResource(location);
        return resource.getInputStream();
    }

    public static String readFileToString(File file) throws IOException {
        Scanner scanner = new Scanner(file).useDelimiter("\\Z");
        String contents = scanner.next();
        return contents.replaceAll("[\\n\\t\\r]", "");
    }

    public static void stringToFile(File file, String str) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(str);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Errors occured when writing string to file", e);
        }
    }

    public static House createTestHouse(){
        House house = new House();
        house.setIsLocked(true);

        Kitchen kitchen = new Kitchen();
        kitchen.setIsLocked(true);

        Cupboard cupboard = new Cupboard();
        cupboard.setCapacity(8);
        kitchen.setCupboard(cupboard);

        Fridge fridge = new Fridge();
        fridge.setBoughtOn(Calendar.getInstance());
        fridge.setInnerTemperature(-4);
        kitchen.setFridge(fridge);
        house.setKitchen(kitchen);

        LivingRoom livingRoom = new LivingRoom();
        livingRoom.setIsLocked(true);
        house.setLivingRoom(livingRoom);
        return house;
    }
}
