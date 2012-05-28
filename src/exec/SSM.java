package exec;



import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import base.WCursor;
import base.DCTriple;

/////////////////////////////////////////////////////////////////////////////////
// SSM (System State Manager)
// Stores the system states that may be shared across different modules 
// for example: 
//   - currently focused group object
//   - the global date range 
//
/////////////////////////////////////////////////////////////////////////////////
public class SSM {
   private static SSM instance;
   
   ////////////////////////////////////////////////////////////////////////////////
   // Default constructor
   //   Just make sure all the variable have some sort of default value
   ////////////////////////////////////////////////////////////////////////////////
   protected SSM() {
      dirty = 0; 
      dirtyGL = 0;
      selectedGroup = new Hashtable<Integer, Integer>();
      startTimeFrame = "19950101";  // yyyyMMdd
      endTimeFrame   = "19951231";  // yyyyMMdd
      startYear = 1995;
      endYear = 1995;
      startMonth = 0;
      endMonth = 0;
      
      renderSihoulette = false;
      mouseX = 0;
      mouseY = 0;
      refreshMagicLens = true;
      refreshOITBuffers = true;
      occlusionLevel = 0;
      useGuide = false;
      useCircularLabel = false;
      relatedList = new Vector<Integer>();
      
      
      // Parse runtime parameters
      useTUIO         = Boolean.parseBoolean(System.getProperty("UseTUIO", "true"));
      useFullScreen   = Boolean.parseBoolean(System.getProperty("UseFullScreen", "false"));
      refreshRate     = Long.parseLong(System.getProperty("RefreshRate", "800"));
      nearThreshold   = Float.parseFloat(System.getProperty("NearThreshold", "0.2f")); 
      downsampleRate  = Integer.parseInt(System.getProperty("DownsampleRate", "3"));
      
      
   }
   
   
   public static SSM instance() {
      if (instance == null) instance = new SSM();
      return instance;
   }
   
   
   // Runtime parameters
   public static boolean useTUIO = false;
   public static boolean useFullScreen = false;
   public static long refreshRate = 800L;
   public static float nearThreshold = 0.2f;
   public static int downsampleRate = 3;
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Reset data
   // - destroy all lens
   // - reset near far and fov 
   ////////////////////////////////////////////////////////////////////////////////
   public void reset() {
      fov = 30.0f;   
      nearPlane = 1.0f;
      farPlane = 1000.0f;
      
      dirtyGL = 1;
      dirty = 1;
      refreshMagicLens = true;
      refreshOITBuffers = true;
      
      
      
      selectedGroup = new Hashtable<Integer, Integer>();
      relatedList.clear();
      
      // Clear buffers
      pickPoints.clear();
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Thanks to IEEE 754 ... we need an EPS number ... thanks to Java...this
   // needs to be a double .... sigh...
   ////////////////////////////////////////////////////////////////////////////////
   public static double EPS = 1E-4;
   
   
   
   ///////////////////////////////////////////////////////////////////////////////// 
   // 3D perspective variables
   ///////////////////////////////////////////////////////////////////////////////// 
   public static float fov = 30.0f;
   public static float nearPlane = 1.0f;
   public static float farPlane  = 1000.0f; 
   
   public static boolean  renderSihoulette;
   public static boolean  useLight = true;
   public static float rotateX = 0.0f;
   public static float rotateY = 0.0f;
   
   
//   
   // Need to sync this because graphics
   // and logic are running in different threads ... thanks Java !!!
//   public synchronized void setCurrentGroup(Integer group) {
//      currentGroup = group;   
//   }
//   
   
   ///////////////////////////////////////////////////////////////////////////////// 
   // Indicators for graphic effects
   ///////////////////////////////////////////////////////////////////////////////// 
   
   public static int LEN_TEXTURE_WIDTH = 800;   
   public static int LEN_TEXTURE_HEIGHT = 800;
   public static boolean refreshMagicLens;
   public static boolean refreshOITBuffers = true;
   public static boolean refreshOITTexture = true;
   public static boolean refreshGlowTexture = true;
   
   //public short NUM_LENS = 1;   
   
   
   ///////////////////////////////////////////////////////////////////////////////// 
   // Hold the coordinates to execute picking
   ///////////////////////////////////////////////////////////////////////////////// 
   public static Vector<DCTriple> pickPoints  = new Vector<DCTriple>(0);
   
   public static Hashtable<Long, DCTriple> dragPoints = new Hashtable<Long, DCTriple>(0);
   
   public static Hashtable<Long, DCTriple> hoverPoints = new Hashtable<Long, DCTriple>();
   
   
   ///////////////////////////////////////////////////////////////////////////////// 
   // Level of Detail 
   ///////////////////////////////////////////////////////////////////////////////// 
   public static int g_numPasses = 4;
   public static int g_numGeoPasses = 0;
   
   public static short stipplePattern = (short)0xF0FA;

   
   ///////////////////////////////////////////////////////////////////////////////// 
   // GUI Environment
   ///////////////////////////////////////////////////////////////////////////////// 
   public static float sparkLineHeight  = 80;
   public static float sparkLineWidth   = 200.0f;
   public static int   sparkLineSegment = 50; 
   
   
   
   ///////////////////////////////////////////////////////////////////////////////// 
   // The current focus group/component
   ///////////////////////////////////////////////////////////////////////////////// 
   public static Hashtable<Integer, Integer> selectedGroup;
   public static Integer occlusionLevel;
   public static Vector<Integer> relatedList; // The list of components that are related to the selected components
   public static int maxOccurrence = Integer.MAX_VALUE;
   public static int minOccurrence = Integer.MIN_VALUE;
   
   public static int stopPicking = 0; // Short circuit logic to exit picking loop
   
   
   // For grid selection
   public int selectedX = -1;
   public int selectedY = -1;
   
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Switches
   ////////////////////////////////////////////////////////////////////////////////
   public static boolean useGuide = false;         // Show various debugging artifacts
   public static boolean useCircularLabel = false; // Whether to laybel the sparklines in a circular pattern 
   public static boolean showLabels = true;        // Whether to show labels at all
   public static boolean captureScreen = false;
   public static int sortingMethod = 0;            // Controls how the components are sorted (with respect to rendering order)
   public static int colouringMethod = 4;
   public static int sparklineMode = 1;
   public static boolean useAggregate = false;        // Whether the occurrence count should crawl the parts hierarchy
   public static boolean useFullTimeLine = true;      // Whether to use the entire timeline for the component chart
   public static boolean useDualDepthPeeling = true;  // Whether to use OIT transparency
   public static boolean useConstantAlpha = false;    // Whether or not to use OIT constant alpha
   public static boolean useGlow = true;
   public static boolean useComparisonMode = false;   // Whether to compare across time lines
   public static boolean useLocalFocus = true;       // Whether to nor to render based on current selected components 
   public static boolean use3DModel = true;          // Whether to use integrated 3D view 
   public static boolean useFlag = true;             // Just a temporary flag to trigger adhoc tests and stuff, not used for real data
   public static boolean useStipple = false;         // Whether to use stippling for the labels
   
   public static boolean checkDragEvent = false; 
   
   public static int chartMode = 1;
   public static final int CHART_MODE_BY_MONTH_MAX     = 1;
   public static final int CHART_MODE_BY_COMPONENT_MAX = 2;
   public static final int CHART_MODE_BY_GLOBAL_MAX    = 3;
   
   
   
   ///////////////////////////////////////////////////////////////////////////////// 
   // Indicates the global selected time period
   ///////////////////////////////////////////////////////////////////////////////// 
   public static String startTimeFrame;
   public static String endTimeFrame;
   public static int startMonth = 0;  // 0 - 11
   public static int endMonth = 0;    // 0 - 11
   public static int startYear = 0;
   public static int endYear = 0;
   public static int startIdx = 0;
   public static int endIdx = 0;
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Just for fun
   // Things that may or may not be useful but seems interesting to do (to me)
   ////////////////////////////////////////////////////////////////////////////////
   public static boolean colourRampReverseAlpha = false; // Whether to inverse the alpha in the colour scale 
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Switch Buttons Management 
   ////////////////////////////////////////////////////////////////////////////////
   public static float aggregationAnchorX = 100f;
   public static float aggregationAnchorY = 100f;
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Handle top elements separately
   // These are activated/de-activated with the mouse press and mouse release 
   // respectively
   ////////////////////////////////////////////////////////////////////////////////
   public static int ELEMENT_NONE = 0;
   public static int ELEMENT_LENS = 1;
   public static int ELEMENT_DOCUMENT = 2;
   public static int ELEMENT_MANUFACTURE_SCROLL = 3;
   public static int ELEMENT_MAKE_SCROLL = 4;
   public static int ELEMENT_MODEL_SCROLL = 5;
   public static int ELEMENT_YEAR_SCROLL = 6;
   public static int ELEMENT_SAVELOAD_SCROLL = 7;
   public static int ELEMENT_FILTER = 8;
   
   public static int ELEMENT_CMANUFACTURE_SCROLL = 13;
   public static int ELEMENT_CMAKE_SCROLL = 14;
   public static int ELEMENT_CMODEL_SCROLL = 15;
   public static int ELEMENT_CYEAR_SCROLL = 16;
   
   public static int topElement = ELEMENT_NONE;
   //public int location   = ELEMENT_NONE; // Horrible hack
   
   
   
   
   ///////////////////////////////////////////////////////////////////////////////// 
   // Window Environment
   ///////////////////////////////////////////////////////////////////////////////// 
   public static int mouseX;
   public static int mouseY;
   public static int oldMouseX;
   public static int oldMouseY;
   public static int windowWidth;
   public static int windowHeight;
   
   
   ///////////////////////////////////////////////////////////////////////////////// 
   // Events - just enums, not bitmasks
   // TODO: this uses a single variable to track mouse events ... this is NOT GOOD....
   // fix this when bored !!!
   ///////////////////////////////////////////////////////////////////////////////// 
   public static boolean l_mouseClicked = false;
   public static boolean r_mouseClicked = false;
   public static boolean l_mousePressed = false;
   public static boolean r_mousePressed = false;
   
   
   
   public static boolean controlKey = false;
   public static boolean shiftKey = false;
   
   
   // Figure out which parts are selected based on a precedence order of :
   // 1 - UI_LAYER
   // 2 - COMPONENT_LAYER
   // Otherwise an action has taken on non interactive element (IE: camera movement)
   public static int UI_LAYER= 1;
   public static int COMPONENT_LAYER= 2;
   //public int currentFocusLayer = 0;
   
   public static int dirty = 0;
   public static int dirtyGL = 0;
   public static int dirtyLoad = 0;
   public static int dirtyDateFilter = 0;
   
   
   public float segmentMax = 0;
   
   // To store the touch points - as a visual aid, it does not have any logic in itself
   // Store this in OpenGL coordinate system ( bottom left is 0,0 )
   public static Hashtable<Long, WCursor> touchPoint = new Hashtable<Long, WCursor> ();
   
}
