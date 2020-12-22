package furqon.io.github.mobilproject;

import android.util.Log;

import java.util.IllegalFormatException;

/**
 * <h1>Ayah reference names</h1>
 * It is a simple, convenience class to make life easier
 * The ayah names are treated differently for audio files and DB verses
 * simply feed the ayah number to get the audio reference name and
 * audio file name to get the number part only
 * <p>
 * <b>Note:</b> You don`t have to use it, unless you are OK
 * creating the same stuff over and over again
 *
 * @author  Ahmad Fuzal, Digital Projects
 * @version 1.0
 * @since   2020-12-20
 */
public class ARG {
    static String suraNumber;
    /**
     * Constructor
     * @param sura_number
     */
    public static void setSuraName(String sura_number) {
        /**
         * @param String sura number
         */
        suraNumber = sura_number;
         }
    /**
     * @param ayah_id
     * @returns String of three digits like 001
     */
    public static String getAyahNameOnly(int ayah_id){


        String retVal = "";
        int tempVal;
        //try to parse the string into integer
        try{
            tempVal = ayah_id;
            if(tempVal<9999){
                retVal = "00"+tempVal;
            }else if(tempVal>9999&&tempVal<99999){
                retVal = "0"+tempVal;
            }else {
                //it is higher than 99
                retVal = String.valueOf(ayah_id);
            }
        }catch (IllegalFormatException ignore){

        }
        return retVal;
    }

    /**
     * @param verse_id int
     * @returns String of six digits like 001001
     */
    public static String makeAyahRefName(int verse_id){

        String rv = "";
        rv = getZeroesFixed(suraNumber).concat(getZeroesFixed(String.valueOf(verse_id)));
        return rv;
    }
    /**
     * @param verse_id string
     * @returns audio file name. eg. 5 -> 001005
     */
    public static String makeAyahRefName(String verse_id){

        String rv = "";
        rv = getZeroesFixed(suraNumber).concat(getZeroesFixed(verse_id));
        return rv;
    }
    /**
     * @param s a digit
     * @returns String of three digits like 001
     */
    private static String getZeroesFixed(String s){

        String retVal = "";
        int tempVal;
        //try to parse the string into integer
        try{
            tempVal = Integer.parseInt(s);
            if(tempVal<10){
                retVal = "00"+tempVal;
            }else if(tempVal>9&&tempVal<100){
                retVal = "0"+tempVal;
            }else {
                //it is higher than 99
                retVal = s;
            }
        }catch (IllegalFormatException ignore){

        }
        Log.d("ARG ", retVal);
        return retVal;
    }

    /**
     * @returns String of three digits like 001
     */
    public static String getSurahNameOnly() {

        String retVal = "";
        int tempVal;
        //try to parse the string into integer
        try{

            tempVal = Integer.parseInt(suraNumber);
            if(tempVal<10){
                retVal = "00"+tempVal;
            }else if(tempVal>9&&tempVal<99){
                retVal = "0"+tempVal;
            }else {
                //it is higher than 99
                retVal = String.valueOf(suraNumber);
            }
        }catch (IllegalFormatException ignore){

        }
        return retVal;
    }

    public static int getAyahNameFromReferenceName(String suraNumber2Play) {
        int verseNumber = Integer.parseInt(suraNumber2Play) % 1000;
        return verseNumber;
    }
}
