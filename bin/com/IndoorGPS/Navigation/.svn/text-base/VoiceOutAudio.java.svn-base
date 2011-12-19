package com.IndoorGPS.Navigation;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import com.IndoorGPS.MapActivity;
import com.IndoorGPS.Utilities;
import com.IndoorGPS.LocalizerBasicClass.ConfigSettings;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.speech.tts.TextToSpeech;

public class VoiceOutAudio {
	
	public enum VoiceInstr
	{
	        Stop,
	        StopRelocalize,
	        GoStraight,
	        Rerouting,
	        TurnLeft,
	        TurnRight,
	        YouAreHere,
	        YouHaveArrived,
	        AdvanceTurnLeft,
	        AdvanceTurnRight,
	        OnYourRight,
	        OnYourLeft,
	        InYourFront,
	        YourPathInfoIs,
	        Please,
	        WalkStraightFor,
	        And,
	        YouWillArriveAtDestination,
	        MoveThruDoor,
	        M1, M2, M3, M4, M5, M6, M7, M8, M9, M10, M15, M20, M25,
	        North, South, East, West,
	        AfterTurnFacing,
	        //TurnTo,
	        //Wall,
	        //Clk10, Clk11, Clk1, Clk2, 
	        //Clk4, Clk5, Clk6, Clk7, Clk8,
	        //OnYour,
	        //Obstacle,
	        TurnBackward,
	        FollowVoiceInstr,
	        ADDITIONAL // empty voice instructions
	    }
	
    private static HashMap<VoiceInstr, Integer> voiceInstrAudioList = new HashMap<VoiceInstr, Integer>();
    private static HashMap<String, Integer> voiceInstrDurationList = new HashMap<String, Integer>();
    
    private static String destinationPlace;

    private static HashMap<String, Integer> voiceAdditionAudioList = new HashMap<String, Integer>();

    private static String voiceInstDurFile;

    public static String errBuffer;


    
    public static void LocateAudioFiles() throws NumberFormatException, IOException
    {
        voiceInstDurFile = ConfigSettings.VI_SUBDIR + "/VIDuration.txt";
        GetDurationsFromFile();
    }

    public static boolean LocateAdditionalAudioFile(String filename)
    {
        if (voiceAdditionAudioList.containsKey(filename))
        {
            return true;
        }
        else
        {
        	return false;
        }
    }

/*    public static boolean LocateDestinationAudioFile(String destination)
    {
        destinationPlace = destination;
        return LocateAdditionalAudioFile(destination);            
    }*/                
    
    //// Play Audio ----- Convert VoiceInstr instr to String and use TTS
    public static String PlayAudio(VoiceInstr instr)
    {
    	String instr_s = null;
    	switch (instr)
    	{
    		case Stop:
    			instr_s = "Stop";
    			break;
    		case StopRelocalize:
    			instr_s = "Please stop for the device to re-localize";
    			break;
    		case GoStraight:
    			instr_s = "Go straight";
    			break;
    		case Rerouting:
    			instr_s = "Please wait for the device to re-route";
    			break;
    		case TurnLeft:
    			instr_s = "Find the way on your left";
    			break;
    		case TurnRight:
    			instr_s = "Find the way on your right";
    			break;
    		case YouAreHere:
    			instr_s = "You are here";
    			break;
    		case YouHaveArrived:
    			instr_s = "You have arrived at";
    			break;
    		case AdvanceTurnLeft:
    			instr_s = "Prepare to turn left in a few steps";
    			break;
    		case AdvanceTurnRight:
    			instr_s = "Prepare to trun right in a few steps";
    			break;
    		case OnYourRight:
    			instr_s = "On your right";
    			break;
    		case OnYourLeft:
    			instr_s = "On your left";
    			break;
    		case InYourFront:
    			instr_s = "In your front";
    			break;
    		case YourPathInfoIs:
    			instr_s = "Your path information is";
    			break;
    		case Please:
    			instr_s = "Please";
    			break;
    		case WalkStraightFor:
    			instr_s = "Walk straight for";
    			break;
    		case And:
    			instr_s = "And";
    			break;
    		case YouWillArriveAtDestination:
    			instr_s = "You will arrive at the destination";
    			break;
    		case MoveThruDoor:
    			instr_s = "Move through the door";
    			break;
    		case FollowVoiceInstr:
    			instr_s = "Please follow the voice instructions to reach";
    			break;
    		case AfterTurnFacing:
    			instr_s = "After turn, you are now facing";
    			break;
    		case TurnBackward:
    			instr_s = "Go back";
    			break;
    		case M1:
    			instr_s = "One meter";
    			break;
    		case M2:
    			instr_s = "Two meters";
    			break;    		
    		case M3:
    			instr_s = "Three meters";
    			break; 
    		case M4:
    			instr_s = "Four meters";
    			break;
    		case M5:
    			instr_s = "Five meters";
    			break;
    		case M6:
    			instr_s = "Six meters";
    			break;
    		case M7:
    			instr_s = "Seven meters";
    			break;
    		case M8:
    			instr_s = "Eight meters";
    			break;   			
    		case M9:
    			instr_s = "Nine meters";
    			break;
    		case M10:
    			instr_s = "Ten meters";
    			break;
    		case M15:
    			instr_s = "Fifteen meters";
    			break;
    		case M20:
    			instr_s = "Twenty meters";
    			break;
    		case M25:
    			instr_s = "Twenty five meters";
    			break;
    		case North:
    			instr_s = "North";
    			break;
    		case South:
    			instr_s = "South";
    			break;
    		case West:
    			instr_s = "West";
    			break;
    		case East:
    			instr_s = "East";
    			break;
    		case ADDITIONAL:
    			instr_s = "Aditional";
    			break;   		
    	}
    	
    	return instr_s;
    }

    public static int VoiceDurationCheck(VoiceInstr instr)
    {
    	return voiceInstrDurationList.containsKey(instr.toString()) ? voiceInstrDurationList.get(instr.toString()) : ConfigSettings.DEFAULTVOICEDURATION;
    }
    
    public static String PlayAdditionalAudio(String instr)
    {
		String instr_s = null;
		if(instr.equalsIgnoreCase("DESCRIP_Fountain_L")){
			instr_s = "Fountain is on your left";
		}
		else if(instr.equalsIgnoreCase("DESCRIP_Fountain_R")){
			instr_s = "Fountain is on your right";
		}
		else if(instr.equalsIgnoreCase("DESCRIP_Elevator_L")){
			instr_s = "Elevator is on your left";
		}
		else if(instr.equalsIgnoreCase("DESCRIP_Elevator_R")){
			instr_s = "Elevator is on your right";
		}
		else if(instr.equalsIgnoreCase("DESCRIP_MenWashroom_L")){
			instr_s = "Men's washroom is on your left";
		}
		else if(instr.equalsIgnoreCase("DESCRIP_MenWashroom_R")){
			instr_s = "Men's washroom is on your right";
		}
		else if(instr.equalsIgnoreCase("DESCRIP_WomenWashroom_L")){
			instr_s = "Women's washroom is on your left";
		}
		else if(instr.equalsIgnoreCase("DESCRIP_WomenWashroom_R")){
			instr_s = "Women's washroom is on your right";
		}
		else if(instr.equalsIgnoreCase("DESCRIP_MiniCafe_L")){
			instr_s = "Mini-cafe is on your left";
		}
		else if(instr.equalsIgnoreCase("DESCRIP_MiniCafe_R")){
			instr_s = "Mini-cafe is on your right";
		}
		else if(instr.equalsIgnoreCase("DESCRIP_Stairs_L")){
			instr_s = "Stairs is on your left";
		}
		else if(instr.equalsIgnoreCase("DESCRIP_Stairs_R")){
			instr_s = "Stairs is on your right";
		}
		else if(instr.equalsIgnoreCase("DESCRIP_218C_L")){
			instr_s = "Room 218C is on your left";
		}
		else if(instr.equalsIgnoreCase("DESCRIP_218C_R")){
			instr_s = "Room 218C is on your right";
		}
    	return instr_s;
    	
    }
    
	public static int VoiceDurationAdditionalCheck(String filename)
    {
        //playSound(null, voiceAdditionAudioList.get(filename));
        return voiceInstrDurationList.containsKey(filename) ? voiceInstrDurationList.get(filename) : ConfigSettings.DEFAULTVOICEDURATION;
    }       

    public static int PlayDestinationAudio()
    {
    	//String speech11 = PlayAdditionalAudio(destinationPlace);
    	//MapActivity.Speak(speech11, TextToSpeech.QUEUE_FLUSH);
    	return VoiceDurationAdditionalCheck(destinationPlace);
    }

    
/*    public static void playSound(Context context, Integer sound) 
    {
    	
		AudioManager mgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		int streamVolume = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
		soundpool.play(sound, streamVolume, streamVolume, 1, 0, 1f);
	}*/
    
    /****************************************************************************************************************
    * Private Functions
     * @throws IOException 
     * @throws NumberFormatException 
    * *************************************************************************************************************/


    private static void GetDurationsFromFile() throws NumberFormatException, IOException
    {
        voiceInstrDurationList.clear();

        if (Utilities.IsValidFilePath(voiceInstDurFile))
        {
            String line;
            String[] splitstrings;

            BufferedReader sr = new BufferedReader(new FileReader(voiceInstDurFile));

            while ((line = sr.readLine()) != null)
            {
                if (line.contains("///")) { continue; }

                splitstrings = line.split(" ");
                voiceInstrDurationList.put(splitstrings[0], Integer.parseInt(splitstrings[1]));
            }
            sr.close();

        }
    }

	
}
