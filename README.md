# Plugin : cucumber-parallel-xbrowser-testing

## Steps to execute this plugin : 

### STEP 1 : Include below mentioned dependencies and plugins in the pom.xml

##### Dependency : cucumber-parallel-xbrowser-testing

```
<dependency>
    <groupId>io.github.rsshekhawat</groupId>
    <artifactId>cucumber-parallel-xbrowser-testing</artifactId>
    <version>0.0.1</version>
</dependency>
```

##### Plugin : cucumber-parallel-xbrowser-testing

```
<plugin>
    <groupId>io.github.rsshekhawat</groupId>
    <artifactId>cucumber-parallel-xbrowser-testing</artifactId>
    <version>0.0.1</version>
    <executions>
      <execution>
        <id>parallel</id>
        <phase>generate-test-resources</phase>
        <goals>
          <goal>xbrowser</goal>
        </goals>
        <configuration>
          <templateRunnerPath>path_to_test_runner_template</templateRunnerPath>
          <featureFilesPath>path_to_feature_files_directory</featureFilesPath>
          <configurationFilePath>path_to_config_file</configurationFilePath>
          <includedTags>@Smoke</includedTags>
        </configuration>
      </execution>
    </executions>
</plugin>
```

##### Plugin : build-helper-maven-plugin

```
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>build-helper-maven-plugin</artifactId>
    <version>3.3.0</version>
    <executions>
      <execution>
        <id>add-test-source</id>
        <phase>generate-test-sources</phase>
        <goals>
          <goal>add-test-source</goal>
        </goals>
        <configuration>
          <sources>
            <source>${project.build.directory}/parallel-xbrowser/runners/</source>
          </sources>
        </configuration>
      </execution>
    </executions>
</plugin>
```

##### Plugin : maven-failsafe-plugin

```
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-failsafe-plugin</artifactId>
    <version>3.0.0-M5</version>
    <executions>
      <execution>
        <goals>
          <goal>integration-test</goal>
          <goal>verify</goal>
        </goals>
        <configuration>
          <forkCount>count</forkCount>
          <reportsDirectory>target/failsafe-reports-${surefire.forkNumber}</reportsDirectory>
        </configuration>
      </execution>
    </executions>
</plugin>
```

### STEP 2 : Create config.xml file to provide the details of different configurations (OS/Browser) on which to execute plugin.

##### Change below tags and configurations according to your projects. You can provide as many configurations as you want to execute tests on different given configurations.

```
<configurations>
    <configuration>
      <browser>Chrome</browser>
      <bSbrowser>Edge</bSbrowser>
      <BSbrowserversion>96</BSbrowserversion>
      <BSos>WINDOWS</BSos>
      <BSosversion>10</BSosversion>
      <seleniumserver>local</seleniumserver>
      <seleniumserverhost>host_address</seleniumserverhost>
    </configuration>

    <configuration>
      <browser>BrowserStack</browser>
      <bSbrowser>Edge</bSbrowser>
      <BSbrowserversion>96</BSbrowserversion>
      <BSos>WINDOWS</BSos>
      <BSosversion>10</BSosversion>
      <seleniumserver>remote</seleniumserver>
      <seleniumserverhost>host_address</seleniumserverhost>
    </configuration>
 </configurations>
  ```
  
  ### STEP 3 : Create xbrowser.template file which is exactly the replica of the test runner you want to run for each configuration
  
 ```  
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import java.io.File;
import java.io.IOException;

// Don't change strings 'TEST_RUNNER_CLASS_NAME', 'FEATURE_FILES_PATH', 'FEATURE_FILES_TAGS'

@CucumberOptions(
        features = {"FEATURE_FILES_PATH"}, // FEATURE_FILES_PATH will pe passed as argument in plugin
        monochrome = true,
        tags = "FEATURE_FILES_TAGS", // FEATURE_FILES_TAGS will be passed as argument in plugin
        glue="", // change this as per your project
        plugin = {"json:target/parallel-xbrowser/cucumber-report/TEST_RUNNER_CLASS_NAME.json"}
)
public class TEST_RUNNER_CLASS_NAME extends AbstractTestNGCucumberTests {

    @BeforeClass
    public void init() throws IOException {
        
        // Change below given code section as per your needs. This is just to create a 'data' directory for storing data.properties files

        //----------------------------------------------------
        String directoryPath = System.getProperty("user.dir")+File.separator+"target"+File.separator+"parallel-xbrowser"+File.separator+"data";
        String filePath = directoryPath + File.separator + "TEST_RUNNER_CLASS_NAME.properties";
        File dir = new File(directoryPath);
        if (!dir.exists()) dir.mkdirs();
        File file = new File(filePath);
        boolean flag = file.createNewFile();
        Reporter.log("New file created : "+flag);
        PropFileHandler.filePath = filePath; 
        // PropFileHandler is the class for writing temprary data into data.properties. Change this as per your project.
        //-----------------------------------------------------
        
        setSystemVariables();
    }

    public void setSystemVariables(){

		  // Set few system variables as per your project to run the tests on browserstack or VM
		  // You can set system variables as per your needs. 
		  // Following is a way to set one system property :  
		  // System.setProperty("property","propertyValue") 
		  // Note : This "property" should be same as one of the property tags in 'configuration' tag in config.xml file
		  // Note : propertyValue = "property" + "Value"  // "Value" is just a string
		  // see below examples for reference
		  
         System.setProperty("browser","browserValue"); // browser
         System.setProperty("BSbrowser","BSbrowserValue"); //browserstack browser
         System.setProperty("BSbrowserversion","BSbrowserversionValue"); // browserstack browser version
         System.setProperty("BSos","BSosValue"); // browserstack OS
         System.setProperty("BSosversion","BSosversionValue"); //browserstack OS version
         System.setProperty("seleniumserver","seleniumserverValue"); // selenium server = local/remote}
         System.setProperty("seleniumserverhost","seleniumserverhostValue");
    }

    @AfterClass
    public void closeSession(){
	
		//Write your code here to close the driver as per your project
        Reporter.log("Closing browser",true);
        try {
            driver.quit();
        }catch(Exception exception){
            Reporter.log(exception.getMessage());
        }
    }
}
 ```
 
 ### STEP 4 : Execute plugin with mvn command
 
 ```
 mvn clean verify
 ```
