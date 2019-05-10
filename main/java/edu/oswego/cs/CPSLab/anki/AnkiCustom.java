package edu.oswego.cs.CPSLab.anki;

import de.adesso.anki.AnkiConnector;
import de.adesso.anki.MessageListener;
import de.adesso.anki.Vehicle;
import de.adesso.anki.messages.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AnkiCustom {

    static long pingReceivedAt;
    static long pingSentAt;


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
                // Binding Vehicle from List into Vehicle object container
                Vehicle car = iter.next();

                System.out.println("\nNow connecting to and doing stuff to your cars.\n\n");

                System.out.println("\nConnecting to " + car + " @ " + car.getAddress());
                car.connect();

                System.out.println("   Connected. Setting SDK mode...");   //always set the SDK mode FIRST!
                car.sendMessage(new SdkModeMessage());
                System.out.println("   SDK Mode set.");

                System.out.println("   Setting Speed...");
                car.sendMessage(new SetSpeedMessage(250, 100));

                // NEW SHIT
                System.out.println("   Getting Position Information...");
                PositionResponseHandler positionLog = new PositionResponseHandler();
                car.addMessageListener(LocalizationPositionUpdateMessage.class, positionLog);


                Thread.sleep(TimeUnit.MILLISECONDS.toDays(30)); // Time for the whole car to drive
                if (positionLog.asyncGetRoadPieceID() == 10) {
                    car.sendMessage(new SetSpeedMessage(0, 0));
                    Thread.sleep(TimeUnit.MILLISECONDS.toSeconds(3)); // Stop at intersection
                } else {
                    for(int i = 0;  i < 50; i++){
                    System.out.println("ELSE BLOCK HIT: FAILURE");
                   // System.out.println("disconnected from " + car + "\n");
                }}
            }
        }
        anki.close();
        System.exit(0);
    }
    // Response Handlers classes in order to get message updates asynchronously

    /**
     * Handles the response from the vehicle from the PingRequestMessage.
     * We need handler classes because responses from the vehicles are asynchronous.
     */
    private static class PositionResponseHandler implements MessageListener<LocalizationPositionUpdateMessage> {
        private LocalizationPositionUpdateMessage LPUM;
        @Override
        public void messageReceived(LocalizationPositionUpdateMessage message) {
            System.out.println("--------------------------------------------------");
            System.out.println("Position is: " + message);
            System.out.println("--------------------------------------------------");
            System.out.println("SINGLE ROAD PIECE ID: " + message.getRoadPieceId());
        }

        public int asyncGetRoadPieceID() {
           return  LPUM.getRoadPieceId();
        }
    }
}
