package briefj.tomove;

import java.io.File;
import java.util.Collection;

import binc.Command;
import briefj.BriefStrings;



/**
 * Manages a pool of experimental results.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class Results
{
  /**
   * Main point of entry: this will return a directory unique
   * to this execution.
   * 
   * Located in experiments/all/[name of main class]-[unique-id].exec/
   * 
   * A shortcut for the latest execution is also updated in experiments/latest/
   * 
   * @return
   */
  public static File getResultFolder()
  {
    if (resultFolder != null)
      return resultFolder;
    // check if the working dir is already a result folder
    if (isWorkingDirectoryAResultFolder())
      resultFolder = new File(".");
    else
      resultFolder = initResultFolder();
    return resultFolder;
    
  }
  
  public static File getFileInResultFolder(String fileName)
  {
    return new File(getResultFolder(), fileName);
  }
  
  private static File resultFolder = null;
  
  private static File initResultFolder()
  {
    // get results pool folder
    File poolFolder = getPoolFolder();
    
    // create a new
    File result = createResultFolder(poolFolder);
    
    // refresh recent softlinks
    refreshSoftlinks(poolFolder, result);
    
    return result;
  }

  private static void refreshSoftlinks(File poolFolder, File result)
  {
    final String latestString = "latest";
    File latestFolderSoftLink = new File(poolFolder, latestString);
    if (latestFolderSoftLink.exists())
      latestFolderSoftLink.delete();
    
    try { Command.call(Command.cmd("ln").withArgs("-s " + result.getAbsolutePath() + " " + latestFolderSoftLink)); }
    catch (Exception e) {}
  }

  private static File createResultFolder(File poolFolder)
  {
    String name = getMainClassString() + "-" + BriefStrings.generateUniqueId() + ".exec";
    File allResults = new File(poolFolder, "all");
    allResults.mkdir();
    File result = new File(allResults, name);
    result.mkdir();
    new File(result, resultFolderMark).mkdir();
    
    return result;
  }

  private static File getPoolFolder()
  {
    File resultsFolder = new File("results");
    
    if (!resultsFolder.exists())
      resultsFolder.mkdir();
    if (!resultsFolder.isDirectory())
      throw new RuntimeException();
    
    return resultsFolder;
  }

  private static boolean isWorkingDirectoryAResultFolder()
  {
    return (new File(resultFolderMark).exists());
  }
  
  private static String resultFolderMark = ".resultFolderMetadata";
  
  public static String getMainClassString()
  {
    try
    {
      Class<?> mainClass = getMainClass();
      return mainClass.getCanonicalName();
    }
    catch (Exception e)
    {
      return "unknownClass";
    }
  }
  
  private static Class<?> mainClass;
  public static Class<?> getMainClass() 
  {
    if (mainClass != null)
      return mainClass;

    Collection<StackTraceElement[]> stacks = Thread.getAllStackTraces().values();
    for (StackTraceElement[] currStack : stacks) 
    {
      if (currStack.length==0)
        continue;
      StackTraceElement lastElem = currStack[currStack.length - 1];
      if (lastElem.getMethodName().equals("main")) 
      {
        try 
        {
          String mainClassName = lastElem.getClassName();
          mainClass = Class.forName(mainClassName);
          return mainClass;
        } 
        catch (ClassNotFoundException e) 
        {
          throw new RuntimeException();
        }
      }
    }
    throw new RuntimeException();
  }
  
  
  
  public static void main(String [] args)
  {
    File result = getResultFolder();
    System.out.println(result);
    File test = new File(result, "" + System.currentTimeMillis());
    test.mkdir();
  }

  public static File getFolderInResultFolder(String string)
  {
    File result = getFileInResultFolder(string);
    result.mkdirs();
    return result;
  }
}