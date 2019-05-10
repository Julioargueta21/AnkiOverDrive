package edu.oswego.cs.CPSLab.anki;

import de.adesso.anki.AnkiConnector;
import de.adesso.anki.MessageListener;
import de.adesso.anki.Vehicle;
import de.adesso.anki.messages.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * A simple test program to test a connection to your Anki 'Supercars' and 'Supertrucks' using the NodeJS Bluetooth gateway.
 * Simple follow the installation instructions at http://github.com/adessoAG/anki-drive-java, build this project, start the
 * bluetooth gateway using ./gradlew server, and run this class.
 *
 * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
 */
public class AnkiCustom {

    static long pingReceivedAt;
    static long pingSentAt;
    static Vehicle car;

    public static void main(String[] args) throws IOException, InterruptedException {

        System.out.println("Launching connector...");
        AnkiConnector anki = new AnkiConnector("localhost", 5000);
        System.out.print("...looking for cars...");
        List<Vehicle> vehicles = anki.findVehicles();

        if (vehicles.isEmpty()) {
            System.out.println(" NO CARS FOUND. I guess that means we're done.");

        } else {
            System.out.println(" FOUND " + vehicles.size() + " CARS! They are:");

            Iterator<Vehicle> iter = vehicles.iterator();
            while (iter.hasNext()) {
                Vehicle v = iter.next();
                // This outputs it Advert Info

               /* System.out.println("   " + v);
                System.out.println("      ID: " + v.getAdvertisement().getIdentifier());
                System.out.println("      Model: " + v.getAdvertisement().getModel());
                System.out.println("      Model ID: " + v.getAdvertisement().getModelId());
                System.out.println("      Product ID: " + v.getAdvertisement().getProductId());
                System.out.println("      Address: " + v.getAddress());
                System.out.println("      Color: " + v.getColor());
                System.out.println("      charging? " + v.getAdvertisement().isCharging());*/
            }

            iter = vehicles.iterator();
            while (iter.hasNext()) {
                car = iter.next();
                System.out.println("\nNow connecting to and doing stuff to your cars.\n\n");

                System.out.println("\nConnecting to " + car + " @ " + car.getAddress());
                car.connect();
                System.out.println("   Connected. Setting SDK mode...");   //always set the SDK mode FIRST!
                car.sendMessage(new SdkModeMessage());
                System.out.println("   SDK Mode set.");

                System.out.println("   Sending Ping Request...");
                //again, some async set-up required...
                PingResponseHandler prh = new PingResponseHandler();
                car.addMessageListener(PingResponseMessage.class, prh);
                AnkiCustom.pingSentAt = System.currentTimeMillis();
                car.sendMessage(new PingRequestMessage());

                System.out.println("   Setting Speed...");
                car.sendMessage(new SetSpeedMessage(250, 100));

                // System.out.print("Sleeping for 10secs... ");
                //Thread.sleep(10000);


                // NEW SHIT
                System.out.println("   Getting Position Information...");
                PositionResponseHandler posLis = new PositionResponseHandler();
                car.addMessageListener(LocalizationPositionUpdateMessage.class, posLis);


                System.out.println("Entering Thread Sleep ELSE BLOCK");
                Thread.sleep(10000);
                car.disconnect();
                System.out.println("disconnected from " + car + "\n");


            }
        }
        anki.close();
        System.exit(0);
    }



    /**
     * Handles the response from the vehicle from the PingRequestMessage.
     * We need handler classes because responses from the vehicles are asynchronous.
     */
    private static class PingResponseHandler implements MessageListener<PingResponseMessage> {
        @Override
        public void messageReceived(PingResponseMessage m) {
            AnkiCustom.pingReceivedAt = System.currentTimeMillis();
            System.out.println("   Ping response received. Roundtrip: " + (AnkiCustom.pingReceivedAt - AnkiCustom.pingSentAt) + " msec.");
        }
    }

    private static class PositionResponseHandler implements MessageListener<LocalizationPositionUpdateMessage>{
        @Override
        public void messageReceived(LocalizationPositionUpdateMessage message) {
            System.out.println("Position is: "  + message);
        }
    }
}
