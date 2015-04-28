/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nu.nethome.home.items.timer;

import com.jtheory.jdring.AlarmEntry;
import static java.lang.Compiler.command;
import java.util.LinkedList;
import java.util.List;
import nu.nethome.home.item.ExecutionFailure;
import nu.nethome.home.system.HomeService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author peter
 */
// @Ignore
public class DayLiteTimerTest {

    public DayLiteTimerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getTimezoneIdentifier method, of class DayLiteTimer.
     */
    @Test
    public void testGetTimezoneIdentifier() {
        System.out.println("getTimezoneIdentifier");
        DayLiteTimer instance = new DayLiteTimer();
        instance.setTimezoneIdentifier("Europe/Stockholm");
        String expResult = "Europe/Stockholm";
        String result = instance.getTimezoneIdentifier();
        assertEquals(expResult, result);
    }

    /**
     * Test of setTimezoneIdentifier method, of class DayLiteTimer.
     */
    @Test
    public void testSetTimezoneIdentifier() {
        System.out.println("setTimezoneIdentifier");
        String timezoneIdentifier = "";
        DayLiteTimer instance = new DayLiteTimer();
        instance.setTimezoneIdentifier(timezoneIdentifier);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of activate method, of class DayLiteTimer.
     */
    @Test
    public void testActivate() {
        System.out.println("activate");
        HomeService server = null;
        DayLiteTimer instance = new DayLiteTimer();
        instance.activate(server);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getState method, of class DayLiteTimer.
     */
    @Test
    public void testGetState() {
        System.out.println("getState");
        DayLiteTimer instance = new DayLiteTimer();
        String expResult = "";
        String result = instance.getState();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setState method, of class DayLiteTimer.
     */
    @Test
    public void testSetState() {
        System.out.println("setState");
        String state = "";
        DayLiteTimer instance = new DayLiteTimer();
        instance.setState(state);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of stop method, of class DayLiteTimer.
     */
    @Test
    public void testStop() {
        System.out.println("stop");
        DayLiteTimer instance = new DayLiteTimer();
        instance.stop();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getModelOld method, of class DayLiteTimer.
     */
    @Test
    public void testGetModelOld() {
        System.out.println("getModelOld");
        DayLiteTimer instance = new DayLiteTimer();
        String expResult = "<?xml version = \"1.0\"?>  <HomeItem Class=\"DayLiteTimer\" Category=\"Timers\" >  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Init=\"setState\" Default=\"true\" />  <Attribute Name=\"Timezone identifier\" Type=\"StringList\" Get=\"getTimezoneIdentifier\" 	Set=\"setTimezoneIdentifier\">		<item>Africa/Abidjan</item>		<item>Africa/Accra</item>		<item>Africa/Addis_Ababa</item>		<item>Africa/Algiers</item>		<item>Africa/Asmara</item>		<item>Africa/Asmera</item>		<item>Africa/Bamako</item>		<item>Africa/Bangui</item>		<item>Africa/Banjul</item>		<item>Africa/Bissau</item>		<item>Africa/Blantyre</item>		<item>Africa/Brazzaville</item>		<item>Africa/Bujumbura</item>		<item>Africa/Cairo</item>		<item>Africa/Casablanca</item>		<item>Africa/Ceuta</item>		<item>Africa/Conakry</item>		<item>Africa/Dakar</item>		<item>Africa/Dar_es_Salaam</item>		<item>Africa/Djibouti</item>		<item>Africa/Douala</item>		<item>Africa/El_Aaiun</item>		<item>Africa/Freetown</item>		<item>Africa/Gaborone</item>		<item>Africa/Harare</item>		<item>Africa/Johannesburg</item>		<item>Africa/Juba</item>		<item>Africa/Kampala</item>		<item>Africa/Khartoum</item>		<item>Africa/Kigali</item>		<item>Africa/Kinshasa</item>		<item>Africa/Lagos</item>		<item>Africa/Libreville</item>		<item>Africa/Lome</item>		<item>Africa/Luanda</item>		<item>Africa/Lubumbashi</item>		<item>Africa/Lusaka</item>		<item>Africa/Malabo</item>		<item>Africa/Maputo</item>		<item>Africa/Maseru</item>		<item>Africa/Mbabane</item>		<item>Africa/Mogadishu</item>		<item>Africa/Monrovia</item>		<item>Africa/Nairobi</item>		<item>Africa/Ndjamena</item>		<item>Africa/Niamey</item>		<item>Africa/Nouakchott</item>		<item>Africa/Ouagadougou</item>		<item>Africa/Porto-Novo</item>		<item>Africa/Sao_Tome</item>		<item>Africa/Timbuktu</item>		<item>Africa/Tripoli</item>		<item>Africa/Tunis</item>		<item>Africa/Windhoek</item>		<item>America/Adak</item>		<item>America/Anchorage</item>		<item>America/Anguilla</item>		<item>America/Antigua</item>		<item>America/Araguaina</item>		<item>America/Argentina/Buenos_Aires</item>		<item>America/Argentina/Catamarca</item>		<item>America/Argentina/ComodRivadavia</item>		<item>America/Argentina/Cordoba</item>		<item>America/Argentina/Jujuy</item>		<item>America/Argentina/La_Rioja</item>		<item>America/Argentina/Mendoza</item>		<item>America/Argentina/Rio_Gallegos</item>		<item>America/Argentina/Salta</item>		<item>America/Argentina/San_Juan</item>		<item>America/Argentina/San_Luis</item>		<item>America/Argentina/Tucuman</item>		<item>America/Argentina/Ushuaia</item>		<item>America/Aruba</item>		<item>America/Asuncion</item>		<item>America/Atikokan</item>		<item>America/Atka</item>		<item>America/Bahia</item>		<item>America/Bahia_Banderas</item>		<item>America/Barbados</item>		<item>America/Belem</item>		<item>America/Belize</item>		<item>America/Blanc-Sablon</item>		<item>America/Boa_Vista</item>		<item>America/Bogota</item>		<item>America/Boise</item>		<item>America/Buenos_Aires</item>		<item>America/Cambridge_Bay</item>		<item>America/Campo_Grande</item>		<item>America/Cancun</item>		<item>America/Caracas</item>		<item>America/Catamarca</item>		<item>America/Cayenne</item>		<item>America/Cayman</item>		<item>America/Chicago</item>		<item>America/Chihuahua</item>		<item>America/Coral_Harbour</item>		<item>America/Cordoba</item>		<item>America/Costa_Rica</item>		<item>America/Creston</item>		<item>America/Cuiaba</item>		<item>America/Curacao</item>		<item>America/Danmarkshavn</item>		<item>America/Dawson</item>		<item>America/Dawson_Creek</item>		<item>America/Denver</item>		<item>America/Detroit</item>		<item>America/Dominica</item>		<item>America/Edmonton</item>		<item>America/Eirunepe</item>		<item>America/El_Salvador</item>		<item>America/Ensenada</item>		<item>America/Fort_Wayne</item>		<item>America/Fortaleza</item>		<item>America/Glace_Bay</item>		<item>America/Godthab</item>		<item>America/Goose_Bay</item>		<item>America/Grand_Turk</item>		<item>America/Grenada</item>		<item>America/Guadeloupe</item>		<item>America/Guatemala</item>		<item>America/Guayaquil</item>		<item>America/Guyana</item>		<item>America/Halifax</item>		<item>America/Havana</item>		<item>America/Hermosillo</item>		<item>America/Indiana/Indianapolis</item>		<item>America/Indiana/Knox</item>		<item>America/Indiana/Marengo</item>		<item>America/Indiana/Petersburg</item>		<item>America/Indiana/Tell_City</item>		<item>America/Indiana/Vevay</item>		<item>America/Indiana/Vincennes</item>		<item>America/Indiana/Winamac</item>		<item>America/Indianapolis</item>		<item>America/Inuvik</item>		<item>America/Iqaluit</item>		<item>America/Jamaica</item>		<item>America/Jujuy</item>		<item>America/Juneau</item>		<item>America/Kentucky/Louisville</item>		<item>America/Kentucky/Monticello</item>		<item>America/Knox_IN</item>		<item>America/Kralendijk</item>		<item>America/La_Paz</item>		<item>America/Lima</item>		<item>America/Los_Angeles</item>		<item>America/Louisville</item>		<item>America/Lower_Princes</item>		<item>America/Maceio</item>		<item>America/Managua</item>		<item>America/Manaus</item>		<item>America/Marigot</item>		<item>America/Martinique</item>		<item>America/Matamoros</item>		<item>America/Mazatlan</item>		<item>America/Mendoza</item>		<item>America/Menominee</item>		<item>America/Merida</item>		<item>America/Metlakatla</item>		<item>America/Mexico_City</item>		<item>America/Miquelon</item>		<item>America/Moncton</item>		<item>America/Monterrey</item>		<item>America/Montevideo</item>		<item>America/Montreal</item>		<item>America/Montserrat</item>		<item>America/Nassau</item>		<item>America/New_York</item>		<item>America/Nipigon</item>		<item>America/Nome</item>		<item>America/Noronha</item>		<item>America/North_Dakota/Beulah</item>		<item>America/North_Dakota/Center</item>		<item>America/North_Dakota/New_Salem</item>		<item>America/Ojinaga</item>		<item>America/Panama</item>		<item>America/Pangnirtung</item>		<item>America/Paramaribo</item>		<item>America/Phoenix</item>		<item>America/Port-au-Prince</item>		<item>America/Port_of_Spain</item>		<item>America/Porto_Acre</item>		<item>America/Porto_Velho</item>		<item>America/Puerto_Rico</item>		<item>America/Rainy_River</item>		<item>America/Rankin_Inlet</item>		<item>America/Recife</item>		<item>America/Regina</item>		<item>America/Resolute</item>		<item>America/Rio_Branco</item>		<item>America/Rosario</item>		<item>America/Santa_Isabel</item>		<item>America/Santarem</item>		<item>America/Santiago</item>		<item>America/Santo_Domingo</item>		<item>America/Sao_Paulo</item>		<item>America/Scoresbysund</item>		<item>America/Shiprock</item>		<item>America/Sitka</item>		<item>America/St_Barthelemy</item>		<item>America/St_Johns</item>		<item>America/St_Kitts</item>		<item>America/St_Lucia</item>		<item>America/St_Thomas</item>		<item>America/St_Vincent</item>		<item>America/Swift_Current</item>		<item>America/Tegucigalpa</item>		<item>America/Thule</item>		<item>America/Thunder_Bay</item>		<item>America/Tijuana</item>		<item>America/Toronto</item>		<item>America/Tortola</item>		<item>America/Vancouver</item>		<item>America/Virgin</item>		<item>America/Whitehorse</item>		<item>America/Winnipeg</item>		<item>America/Yakutat</item>		<item>America/Yellowknife</item>		<item>Antarctica/Casey</item>		<item>Antarctica/Davis</item>		<item>Antarctica/DumontDUrville</item>		<item>Antarctica/Macquarie</item>		<item>Antarctica/Mawson</item>		<item>Antarctica/McMurdo</item>		<item>Antarctica/Palmer</item>		<item>Antarctica/Rothera</item>		<item>Antarctica/South_Pole</item>		<item>Antarctica/Syowa</item>		<item>Antarctica/Vostok</item>		<item>Arctic/Longyearbyen</item>		<item>Asia/Aden</item>		<item>Asia/Almaty</item>		<item>Asia/Amman</item>		<item>Asia/Anadyr</item>		<item>Asia/Aqtau</item>		<item>Asia/Aqtobe</item>		<item>Asia/Ashgabat</item>		<item>Asia/Ashkhabad</item>		<item>Asia/Baghdad</item>		<item>Asia/Bahrain</item>		<item>Asia/Baku</item>		<item>Asia/Bangkok</item>		<item>Asia/Beirut</item>		<item>Asia/Bishkek</item>		<item>Asia/Brunei</item>		<item>Asia/Calcutta</item>		<item>Asia/Choibalsan</item>		<item>Asia/Chongqing</item>		<item>Asia/Chungking</item>		<item>Asia/Colombo</item>		<item>Asia/Dacca</item>		<item>Asia/Damascus</item>		<item>Asia/Dhaka</item>		<item>Asia/Dili</item>		<item>Asia/Dubai</item>		<item>Asia/Dushanbe</item>		<item>Asia/Gaza</item>		<item>Asia/Harbin</item>		<item>Asia/Hebron</item>		<item>Asia/Ho_Chi_Minh</item>		<item>Asia/Hong_Kong</item>		<item>Asia/Hovd</item>		<item>Asia/Irkutsk</item>		<item>Asia/Istanbul</item>		<item>Asia/Jakarta</item>		<item>Asia/Jayapura</item>		<item>Asia/Jerusalem</item>		<item>Asia/Kabul</item>		<item>Asia/Kamchatka</item>		<item>Asia/Karachi</item>		<item>Asia/Kashgar</item>		<item>Asia/Kathmandu</item>		<item>Asia/Katmandu</item>		<item>Asia/Khandyga</item>		<item>Asia/Kolkata</item>		<item>Asia/Krasnoyarsk</item>		<item>Asia/Kuala_Lumpur</item>		<item>Asia/Kuching</item>		<item>Asia/Kuwait</item>		<item>Asia/Macao</item>		<item>Asia/Macau</item>		<item>Asia/Magadan</item>		<item>Asia/Makassar</item>		<item>Asia/Manila</item>		<item>Asia/Muscat</item>		<item>Asia/Nicosia</item>		<item>Asia/Novokuznetsk</item>		<item>Asia/Novosibirsk</item>		<item>Asia/Omsk</item>		<item>Asia/Oral</item>		<item>Asia/Phnom_Penh</item>		<item>Asia/Pontianak</item>		<item>Asia/Pyongyang</item>		<item>Asia/Qatar</item>		<item>Asia/Qyzylorda</item>		<item>Asia/Rangoon</item>		<item>Asia/Riyadh</item>		<item>Asia/Saigon</item>		<item>Asia/Sakhalin</item>		<item>Asia/Samarkand</item>		<item>Asia/Seoul</item>		<item>Asia/Shanghai</item>		<item>Asia/Singapore</item>		<item>Asia/Taipei</item>		<item>Asia/Tashkent</item>		<item>Asia/Tbilisi</item>		<item>Asia/Tehran</item>		<item>Asia/Tel_Aviv</item>		<item>Asia/Thimbu</item>		<item>Asia/Thimphu</item>		<item>Asia/Tokyo</item>		<item>Asia/Ujung_Pandang</item>		<item>Asia/Ulaanbaatar</item>		<item>Asia/Ulan_Bator</item>		<item>Asia/Urumqi</item>		<item>Asia/Ust-Nera</item>		<item>Asia/Vientiane</item>		<item>Asia/Vladivostok</item>		<item>Asia/Yakutsk</item>		<item>Asia/Yekaterinburg</item>		<item>Asia/Yerevan</item>		<item>Atlantic/Azores</item>		<item>Atlantic/Bermuda</item>		<item>Atlantic/Canary</item>		<item>Atlantic/Cape_Verde</item>		<item>Atlantic/Faeroe</item>		<item>Atlantic/Faroe</item>		<item>Atlantic/Jan_Mayen</item>		<item>Atlantic/Madeira</item>		<item>Atlantic/Reykjavik</item>		<item>Atlantic/South_Georgia</item>		<item>Atlantic/St_Helena</item>		<item>Atlantic/Stanley</item>		<item>Australia/ACT</item>		<item>Australia/Adelaide</item>		<item>Australia/Brisbane</item>		<item>Australia/Broken_Hill</item>		<item>Australia/Canberra</item>		<item>Australia/Currie</item>		<item>Australia/Darwin</item>		<item>Australia/Eucla</item>		<item>Australia/Hobart</item>		<item>Australia/LHI</item>		<item>Australia/Lindeman</item>		<item>Australia/Lord_Howe</item>		<item>Australia/Melbourne</item>		<item>Australia/NSW</item>		<item>Australia/North</item>		<item>Australia/Perth</item>		<item>Australia/Queensland</item>		<item>Australia/South</item>		<item>Australia/Sydney</item>		<item>Australia/Tasmania</item>		<item>Australia/Victoria</item>		<item>Australia/West</item>		<item>Australia/Yancowinna</item>		<item>Brazil/Acre</item>		<item>Brazil/DeNoronha</item>		<item>Brazil/East</item>		<item>Brazil/West</item>		<item>CET</item>		<item>CST6CDT</item>		<item>Canada/Atlantic</item>		<item>Canada/Central</item>		<item>Canada/East-Saskatchewan</item>		<item>Canada/Eastern</item>		<item>Canada/Mountain</item>		<item>Canada/Newfoundland</item>		<item>Canada/Pacific</item>		<item>Canada/Saskatchewan</item>		<item>Canada/Yukon</item>		<item>Chile/Continental</item>		<item>Chile/EasterIsland</item>		<item>Cuba</item>		<item>EET</item>		<item>EST5EDT</item>		<item>Egypt</item>		<item>Eire</item>		<item>Etc/GMT</item>		<item>Etc/GMT+0</item>		<item>Etc/GMT+1</item>		<item>Etc/GMT+10</item>		<item>Etc/GMT+11</item>		<item>Etc/GMT+12</item>		<item>Etc/GMT+2</item>		<item>Etc/GMT+3</item>		<item>Etc/GMT+4</item>		<item>Etc/GMT+5</item>		<item>Etc/GMT+6</item>		<item>Etc/GMT+7</item>		<item>Etc/GMT+8</item>		<item>Etc/GMT+9</item>		<item>Etc/GMT-0</item>		<item>Etc/GMT-1</item>		<item>Etc/GMT-10</item>		<item>Etc/GMT-11</item>		<item>Etc/GMT-12</item>		<item>Etc/GMT-13</item>		<item>Etc/GMT-14</item>		<item>Etc/GMT-2</item>		<item>Etc/GMT-3</item>		<item>Etc/GMT-4</item>		<item>Etc/GMT-5</item>		<item>Etc/GMT-6</item>		<item>Etc/GMT-7</item>		<item>Etc/GMT-8</item>		<item>Etc/GMT-9</item>		<item>Etc/GMT0</item>		<item>Etc/Greenwich</item>		<item>Etc/UCT</item>		<item>Etc/UTC</item>		<item>Etc/Universal</item>		<item>Etc/Zulu</item>		<item>Europe/Amsterdam</item>		<item>Europe/Andorra</item>		<item>Europe/Athens</item>		<item>Europe/Belfast</item>		<item>Europe/Belgrade</item>		<item>Europe/Berlin</item>		<item>Europe/Bratislava</item>		<item>Europe/Brussels</item>		<item>Europe/Bucharest</item>		<item>Europe/Budapest</item>		<item>Europe/Busingen</item>		<item>Europe/Chisinau</item>		<item>Europe/Copenhagen</item>		<item>Europe/Dublin</item>		<item>Europe/Gibraltar</item>		<item>Europe/Guernsey</item>		<item>Europe/Helsinki</item>		<item>Europe/Isle_of_Man</item>		<item>Europe/Istanbul</item>		<item>Europe/Jersey</item>		<item>Europe/Kaliningrad</item>		<item>Europe/Kiev</item>		<item>Europe/Lisbon</item>		<item>Europe/Ljubljana</item>		<item>Europe/London</item>		<item>Europe/Luxembourg</item>		<item>Europe/Madrid</item>		<item>Europe/Malta</item>		<item>Europe/Mariehamn</item>		<item>Europe/Minsk</item>		<item>Europe/Monaco</item>		<item>Europe/Moscow</item>		<item>Europe/Nicosia</item>		<item>Europe/Oslo</item>		<item>Europe/Paris</item>		<item>Europe/Podgorica</item>		<item>Europe/Prague</item>		<item>Europe/Riga</item>		<item>Europe/Rome</item>		<item>Europe/Samara</item>		<item>Europe/San_Marino</item>		<item>Europe/Sarajevo</item>		<item>Europe/Simferopol</item>		<item>Europe/Skopje</item>		<item>Europe/Sofia</item>		<item>Europe/Stockholm</item>		<item>Europe/Tallinn</item>		<item>Europe/Tirane</item>		<item>Europe/Tiraspol</item>		<item>Europe/Uzhgorod</item>		<item>Europe/Vaduz</item>		<item>Europe/Vatican</item>		<item>Europe/Vienna</item>		<item>Europe/Vilnius</item>		<item>Europe/Volgograd</item>		<item>Europe/Warsaw</item>		<item>Europe/Zagreb</item>		<item>Europe/Zaporozhye</item>		<item>Europe/Zurich</item>		<item>GB</item>		<item>GB-Eire</item>		<item>GMT</item>		<item>GMT0</item>		<item>Greenwich</item>		<item>Hongkong</item>		<item>Iceland</item>		<item>Indian/Antananarivo</item>		<item>Indian/Chagos</item>		<item>Indian/Christmas</item>		<item>Indian/Cocos</item>		<item>Indian/Comoro</item>		<item>Indian/Kerguelen</item>		<item>Indian/Mahe</item>		<item>Indian/Maldives</item>		<item>Indian/Mauritius</item>		<item>Indian/Mayotte</item>		<item>Indian/Reunion</item>		<item>Iran</item>		<item>Israel</item>		<item>Jamaica</item>		<item>Japan</item>		<item>Kwajalein</item>		<item>Libya</item>		<item>MET</item>		<item>MST7MDT</item>		<item>Mexico/BajaNorte</item>		<item>Mexico/BajaSur</item>		<item>Mexico/General</item>		<item>NZ</item>		<item>NZ-CHAT</item>		<item>Navajo</item>		<item>PRC</item>		<item>PST8PDT</item>		<item>Pacific/Apia</item>		<item>Pacific/Auckland</item>		<item>Pacific/Chatham</item>		<item>Pacific/Chuuk</item>		<item>Pacific/Easter</item>		<item>Pacific/Efate</item>		<item>Pacific/Enderbury</item>		<item>Pacific/Fakaofo</item>		<item>Pacific/Fiji</item>		<item>Pacific/Funafuti</item>		<item>Pacific/Galapagos</item>		<item>Pacific/Gambier</item>		<item>Pacific/Guadalcanal</item>		<item>Pacific/Guam</item>		<item>Pacific/Honolulu</item>		<item>Pacific/Johnston</item>		<item>Pacific/Kiritimati</item>		<item>Pacific/Kosrae</item>		<item>Pacific/Kwajalein</item>		<item>Pacific/Majuro</item>		<item>Pacific/Marquesas</item>		<item>Pacific/Midway</item>		<item>Pacific/Nauru</item>		<item>Pacific/Niue</item>		<item>Pacific/Norfolk</item>		<item>Pacific/Noumea</item>		<item>Pacific/Pago_Pago</item>		<item>Pacific/Palau</item>		<item>Pacific/Pitcairn</item>		<item>Pacific/Pohnpei</item>		<item>Pacific/Ponape</item>		<item>Pacific/Port_Moresby</item>		<item>Pacific/Rarotonga</item>		<item>Pacific/Saipan</item>		<item>Pacific/Samoa</item>		<item>Pacific/Tahiti</item>		<item>Pacific/Tarawa</item>		<item>Pacific/Tongatapu</item>		<item>Pacific/Truk</item>		<item>Pacific/Wake</item>		<item>Pacific/Wallis</item>		<item>Pacific/Yap</item>		<item>Poland</item>		<item>Portugal</item>		<item>ROK</item>		<item>Singapore</item>		<item>SystemV/AST4</item>		<item>SystemV/AST4ADT</item>		<item>SystemV/CST6</item>		<item>SystemV/CST6CDT</item>		<item>SystemV/EST5</item>		<item>SystemV/EST5EDT</item>		<item>SystemV/HST10</item>		<item>SystemV/MST7</item>		<item>SystemV/MST7MDT</item>		<item>SystemV/PST8</item>		<item>SystemV/PST8PDT</item>		<item>SystemV/YST9</item>		<item>SystemV/YST9YDT</item>		<item>Turkey</item>		<item>UCT</item>		<item>US/Alaska</item>		<item>US/Aleutian</item>		<item>US/Arizona</item>		<item>US/Central</item>		<item>US/East-Indiana</item>		<item>US/Eastern</item>		<item>US/Hawaii</item>		<item>US/Indiana-Starke</item>		<item>US/Michigan</item>		<item>US/Mountain</item>		<item>US/Pacific</item>		<item>US/Pacific-New</item>		<item>US/Samoa</item>		<item>UTC</item>		<item>Universal</item>		<item>W-SU</item>		<item>WET</item>		<item>Zulu</item>		<item>EST</item>		<item>HST</item>		<item>MST</item>		<item>ACT</item>		<item>AET</item>		<item>AGT</item>		<item>ART</item>		<item>AST</item>		<item>BET</item>		<item>BST</item>		<item>CAT</item>		<item>CNT</item>		<item>CST</item>		<item>CTT</item>		<item>EAT</item>		<item>ECT</item>		<item>IET</item>		<item>IST</item>		<item>JST</item>		<item>MIT</item>		<item>NET</item>		<item>NST</item>		<item>PLT</item>		<item>PNT</item>		<item>PRT</item>		<item>PST</item>		<item>SST</item>		<item>VST</item>	 </Attribute>  <Attribute Name=\"Location: Lat,Long\" Type=\"String\" Get=\"getLatLong\" 	Set=\"setLatLong\" />  <Attribute Name=\"Timer Today\" Type=\"String\" Get=\"getTodayStartEnd\" />  <Attribute Name=\"Sunrise Today\" Type=\"String\" Get=\"getSunriseToday\" />  <Attribute Name=\"Sunset Today\" Type=\"String\" Get=\"getSunsetToday\" />  <Attribute Name=\"Mondays\" Type=\"String\" Get=\"getMondays\" 	Set=\"setMondays\" />  <Attribute Name=\"Tuesdays\" Type=\"String\" Get=\"getTuesdays\" 	Set=\"setTuesdays\" />  <Attribute Name=\"Wednesdays\" Type=\"String\" Get=\"getWednesdays\" 	Set=\"setWednesdays\" />  <Attribute Name=\"Thursdays\" Type=\"String\" Get=\"getThursdays\" 	Set=\"setThursdays\" />  <Attribute Name=\"Fridays\" Type=\"String\" Get=\"getFridays\" 	Set=\"setFridays\" />  <Attribute Name=\"Saturdays\" Type=\"String\" Get=\"getSaturdays\" 	Set=\"setSaturdays\" />  <Attribute Name=\"Sundays\" Type=\"String\" Get=\"getSundays\" 	Set=\"setSundays\" />  <Attribute Name=\"OnCommand\" Type=\"Command\" Get=\"getOnCommand\" 	Set=\"setOnCommand\" />  <Attribute Name=\"OffCommand\" Type=\"Command\" Get=\"getOffCommand\" 	Set=\"setOffCommand\" />  <Action Name=\"Enable timer\" 	Method=\"enableTimer\" />  <Action Name=\"Disable timer\" 	Method=\"disableTimer\" /></HomeItem> ";
        String resultOld = instance.getModel();
        System.out.println(resultOld);
        String resultNew = instance.getModel();
        System.out.println(resultNew);
        resultNew = resultNew.replaceAll("[\\s\\n]", "");
        resultOld = resultOld.replaceAll("[\\s\\n]", "");
        System.out.println(resultOld);
        System.out.println(resultNew);
        assertEquals(resultOld, resultNew);
        assertEquals(expResult, resultNew);
        assertEquals(expResult, resultOld);
    }

    /**
     * Test of getMondays method, of class DayLiteTimer.
     */
    @Test
    public void testGetMondays() {
        System.out.println("getMondays");
        DayLiteTimer instance = new DayLiteTimer();
        String expResult = "";
        String result = instance.getMondays();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTodayStartEnd method, of class DayLiteTimer.
     */
    @Test
    public void testGetTodayStartEnd() {
        System.out.println("getTodayStartEnd");
        DayLiteTimer instance = new DayLiteTimer();
        String expResult = "";
        String result = instance.getTodayStartEnd();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSunriseToday method, of class DayLiteTimer.
     */
    @Test
    public void testGetSunriseToday() {
        System.out.println("getSunriseToday");
        DayLiteTimer instance = new DayLiteTimer();
        String expResult = "";
        String result = instance.getSunriseToday();
        assertTrue(result.length() > 0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSunsetToday method, of class DayLiteTimer.
     */
    @Test
    public void testGetSunsetToday() {
        System.out.println("getSunsetToday");
        DayLiteTimer instance = new DayLiteTimer();
        String expResult = "";
        String result = instance.getSunsetToday();
        assertTrue(result.length() > 0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setMondays method, of class DayLiteTimer.
     */
    @Test
    public void testSetMondays() throws Exception {
        System.out.println("setMondays");
        String mondays = "";
        DayLiteTimer instance = new DayLiteTimer();
        instance.setMondays(mondays);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTuesdays method, of class DayLiteTimer.
     */
    @Test
    public void testGetTuesdays() {
        System.out.println("getTuesdays");
        DayLiteTimer instance = new DayLiteTimer();
        String expResult = "";
        String result = instance.getTuesdays();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setTuesdays method, of class DayLiteTimer.
     */
    @Test
    public void testSetTuesdays() throws Exception {
        System.out.println("setTuesdays");
        String mTuesdays = "";
        DayLiteTimer instance = new DayLiteTimer();
        instance.setTuesdays(mTuesdays);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getWednesdays method, of class DayLiteTimer.
     */
    @Test
    public void testGetWednesdays() {
        System.out.println("getWednesdays");
        DayLiteTimer instance = new DayLiteTimer();
        String expResult = "";
        String result = instance.getWednesdays();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setWednesdays method, of class DayLiteTimer.
     */
    @Test
    public void testSetWednesdays() throws Exception {
        System.out.println("setWednesdays");
        String wednesdays = "";
        DayLiteTimer instance = new DayLiteTimer();
        instance.setWednesdays(wednesdays);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getThursdays method, of class DayLiteTimer.
     */
    @Test
    public void testGetThursdays() {
        System.out.println("getThursdays");
        DayLiteTimer instance = new DayLiteTimer();
        String expResult = "";
        String result = instance.getThursdays();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setThursdays method, of class DayLiteTimer.
     */
    @Test
    public void testSetThursdays() throws Exception {
        System.out.println("setThursdays");
        String thursdays = "";
        DayLiteTimer instance = new DayLiteTimer();
        instance.setThursdays(thursdays);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFridays method, of class DayLiteTimer.
     */
    @Test
    public void testGetFridays() {
        System.out.println("getFridays");
        DayLiteTimer instance = new DayLiteTimer();
        String expResult = "";
        String result = instance.getFridays();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setFridays method, of class DayLiteTimer.
     */
    @Test
    public void testSetFridays() throws Exception {
        System.out.println("setFridays");
        String fridays = "";
        DayLiteTimer instance = new DayLiteTimer();
        instance.setFridays(fridays);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSaturdays method, of class DayLiteTimer.
     */
    @Test
    public void testGetSaturdays() {
        System.out.println("getSaturdays");
        DayLiteTimer instance = new DayLiteTimer();
        String expResult = "";
        String result = instance.getSaturdays();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSaturdays method, of class DayLiteTimer.
     */
    @Test
    public void testSetSaturdays() throws Exception {
        System.out.println("setSaturdays");
        String saturdays = "";
        DayLiteTimer instance = new DayLiteTimer();
        instance.setSaturdays(saturdays);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSundays method, of class DayLiteTimer.
     */
    @Test
    public void testGetSundays() {
        System.out.println("getSundays");
        DayLiteTimer instance = new DayLiteTimer();
        String expResult = "";
        String result = instance.getSundays();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSundays method, of class DayLiteTimer.
     */
    @Test
    public void testSetSundays() throws Exception {
        System.out.println("setSundays");
        String sundays = "";
        DayLiteTimer instance = new DayLiteTimer();
        instance.setSundays(sundays);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLatLong method, of class DayLiteTimer.
     */
    @Test
    public void testGetLatLong() {
        System.out.println("getLatLong");
        DayLiteTimer instance = new DayLiteTimer();
        String expResult = "";
        String result = instance.getLatLong();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setLatLong method, of class DayLiteTimer.
     */
    @Test
    public void testSetLatLong() {
        System.out.println("setLatLong");
        String latitudeLongitude = "";
        DayLiteTimer instance = new DayLiteTimer();
        instance.setLatLong(latitudeLongitude);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getExceptions method, of class DayLiteTimer.
     */
    @Test
    public void testGetExceptions() {
        System.out.println("getExceptions");
        DayLiteTimer instance = new DayLiteTimer();
        String expResult = "";
        String result = instance.getExceptions();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setExceptions method, of class DayLiteTimer.
     */
    @Test
    public void testSetExceptions() {
        System.out.println("setExceptions");
        String exceptions = "";
        DayLiteTimer instance = new DayLiteTimer();
        instance.setExceptions(exceptions);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of init method, of class DayLiteTimer.
     */
    @Test
    public void testInit() {
        System.out.println("init");
        DayLiteTimer instance = new DayLiteTimer();
        instance.init();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of enableTimer method, of class DayLiteTimer.
     */
    @Test
    public void testEnableTimer() throws Exception {
        System.out.println("enableTimer");
        DayLiteTimer instance = new DayLiteTimer();
        instance.enableTimer();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of disableTimer method, of class DayLiteTimer.
     */
    @Test
    public void testDisableTimer() {
        System.out.println("disableTimer");
        DayLiteTimer instance = new DayLiteTimer();
        instance.disableTimer();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of calculateAlarmEntries method, of class DayLiteTimer.
     */
    @Test
    public void testCalculateAlarmEntries() throws Exception {
        System.out.println("calculateAlarmEntries");
        LinkedList<AlarmEntry> alarms = null;
        String timePeriodsString = "";
        int weekDay = 0;
        DayLiteTimer instance = new DayLiteTimer();
        try {
            instance.calculateAlarmEntries(alarms, timePeriodsString, weekDay);
        } catch (ExecutionFailure e) {
            // Okey!
        } catch (Exception e) {
            fail("Unknwon thrown error: " + e.getMessage());
        }

    }

}
