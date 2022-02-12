# Plugin : cucumber-parallel-xbrowser-testing 
## Latest Version : 0.0.4

## Steps to execute this plugin : 

### STEP 1 : Include below mentioned dependencies and plugins in the pom.xml

##### Dependency : cucumber-parallel-xbrowser-testing

```
<dependency>
    <groupId>io.github.rsshekhawat</groupId>
    <artifactId>cucumber-parallel-xbrowser-testing</artifactId>
    <version>latest_version</version> 
</dependency>
```

##### Plugin : cucumber-parallel-xbrowser-testing

```
<plugin>
    <groupId>io.github.rsshekhawat</groupId>
    <artifactId>cucumber-parallel-xbrowser-testing</artifactId>
    <version>latest_version</version>
    <executions>
      <execution>
        <id>parallel</id>
        <phase>generate-test-resources</phase>
        <goals>
          <goal>xbrowser</goal>
        </goals>
        <configuration>
	  // This is mandatory tag. Give the path of your 'xbrowser.template' file
          <templateRunnerPath>path_to_test_runner_template</templateRunnerPath> 
	  
	  // This is mandatory tag. Give the path of your feature files directory
          <featureFilesPath>path_to_feature_files_directory</featureFilesPath>  
	  
	  // This is mandatory tag. Give the path of your config.xml file
          <configurationFilePath>path_to_config_file</configurationFilePath>    
	  
	  // This is mandatory tag. Leave it empty if you are not using tags like <includedTags></includedTags>
	  // You can also use combinations of tags as well, as per the cucumber tags rules.
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
import io.github.rsshekhawat.CreateTestRunners;

@CucumberOptions(
        features = {"FEATURE_FILES_PATH"},
        monochrome = true,
        tags = "FEATURE_FILES_TAGS",
        glue="",
        plugin = {"json:target/parallel-xbrowser/cucumber-report/TEST_RUNNER_CLASS_NAME.json"}
)
public class TEST_RUNNER_CLASS_NAME extends AbstractTestNGCucumberTests {

    @BeforeClass
    public void init() throws IOException {

	// This code section is not necessary to include
	// ------------------------------------------------------------------------------------------------------------------------------------
        String directoryPath = System.getProperty("user.dir")+File.separator+"target"+File.separator+"parallel-xbrowser"+File.separator+"data";
        String filePath = directoryPath + File.separator + "TEST_RUNNER_CLASS_NAME.properties";
        PropFileHandler.filePath = filePath;
	// ------------------------------------------------------------------------------------------------------------------------------------
	
	// below function will set system properties to run this test runner on given configuration in config.xml
        new CreateTestRunners().setSystemVariables("TEST_RUNNER_CLASS_NAME.properties");
    }

    @AfterClass
    public void closeSession(){

	// Write your own code to close the session as per your project
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
