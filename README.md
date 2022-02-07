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
