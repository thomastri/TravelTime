import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.*;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class TravelTimeSpeechlet implements Speechlet {

    static String stringFromJson(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);

            return buffer.toString();
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
            return null;
        } finally {
            if (reader != null)
                reader.close();
        }
    }

    static String distance() {

        JSONObject mapsData = null;

        try {
            mapsData = mapsAPI();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mapsData.getJSONArray("routes").getJSONObject(0)
                .getJSONArray("legs").getJSONObject(0)
                .getJSONObject("duration").get("text").toString();

    }

    static JSONObject mapsAPI() throws Exception {

        String originPlaceID = "place_id:ChIJuadLEmhntokR9sfSeaH41a4";
        String destinationPlaceID = "place_id:ChIJ4fxJbh9ItokRjVM2N7Mkh50";
        String avoidTolls = "&avoid=tolls";
        String apiKey = "AIzaSyCjkFfA3uxrJ2DSYpQcuOF5KnvETzWBQjs";

        String urlString = "https://maps.googleapis.com/maps/api/directions/json?origin=" + originPlaceID
                + "&destination=" + destinationPlaceID
                + avoidTolls + "&key=" + apiKey;

        return new JSONObject(stringFromJson(urlString));
    }

    // called when session first starts
    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException {
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
            throws SpeechletException {
        return getMapDataResponse();
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException {
        // any cleanup logic goes here
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
            throws SpeechletException {

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        if ("GetMapIntent".equals(intentName)) {
            try {
                return getMapDataResponse();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("AMAZON.HelpIntent".equals(intentName)) {
            return getHelpResponse();

        } else if ("AMAZON.StopIntent".equals(intentName)) {
            PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
            outputSpeech.setText("Stay thirsty, my friends.");

            return SpeechletResponse.newTellResponse(outputSpeech);
        } else if ("AMAZON.CancelIntent".equals(intentName)) {
            PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
            outputSpeech.setText("Stay thirsty, my friends.");

            return SpeechletResponse.newTellResponse(outputSpeech);
        } else {
            throw new SpeechletException("Invalid Intent");
        }

        PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
        outputSpeech.setText("There was an error of some sort. I'm sorry I wasted your time. I'm just a machine.");

        return SpeechletResponse.newTellResponse(outputSpeech);
    }

    private SpeechletResponse getMapDataResponse() {

        String distanceString = "Your current travel time from Home to Work is: " + distance();

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("TravelTime");
        card.setContent(distanceString);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(distanceString);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    private SpeechletResponse getHelpResponse() {
        String speechText =
                "You can ask Travel Time to check on your daily Travel Time for you. Do you want to know?";

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt);
    }

}
