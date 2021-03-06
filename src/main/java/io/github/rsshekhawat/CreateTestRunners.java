package io.github.rsshekhawat;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import org.json.XML;

@Mojo(name="xbrowser", defaultPhase = LifecyclePhase.INTEGRATION_TEST)
public class CreateTestRunners extends AbstractMojo
{
    @Parameter(property = "project", readonly = true)
    private MavenProject mavenProject;

    @Parameter(property = "templateRunnerPath", required = true)
    private String templateRunnerPath;

    @Parameter(property = "featureFilesPath", required = true)
    private String featureFilesPath;

    @Parameter(property = "configurationFilePath", required = true)
    private String configurationFilePath;

    @Parameter(property = "includedTags")
    private String includedTags;

    @Parameter( defaultValue = "${mojoExecution}", readonly = true )
    private MojoExecution mojo;

    String baseDirectory = System.getProperty("user.dir")+ File.separator+"target"+File.separator+"parallel-xbrowser";
    String runnersDirectoryPath = baseDirectory+File.separator+"runners";
    String propertiesDirectoryPath = baseDirectory+File.separator+"properties";
    String dataDirectoryPath = baseDirectory+File.separator+"data";
    public static Map<Integer, Map<String, String>> configMap;
    String fileNamePattern = "IT_Cucumber_Parallel_Cross_Browser_";
    int totalFiles = 0;

    @Override
    public void execute() throws MojoExecutionException {

        getLog().info("------------------------------------------------------------------------");
        getLog().info("Plugin Name : "+mojo.getPlugin().getArtifactId());
        getLog().info("Plugin Version : "+mojo.getPlugin().getVersion());
        getLog().info("Developer : Rahul Shekhawat");
        getLog().info("------------------------------------------------------------------------");
        try {
            configMap = convertXMLToJSONAndReturnMap();
            createTestRunners();
            createPropertiesFilesForEachTestRunner();
            addSpecificsToTestRunners();
            printConfigurationsLogs();
            createDataPropertiesFile();
        } catch (Exception exc) {
            getLog().info("Exception : "+exc.getMessage());
        }
    }

    public void createTestRunners() throws IOException {

        String content = new String(Files.readAllBytes(Paths.get(templateRunnerPath)));
        content = ripOffPackageNameFromJavaFile(content);

        File dir = new File(runnersDirectoryPath);
        if (!dir.exists()) dir.mkdirs();

        for(int i=0;i<totalFiles;i++) {
            String filePath = runnersDirectoryPath + File.separator + fileNamePattern +i+".java";
            File file = new File(filePath);
            boolean flag = file.createNewFile();
            FileWriter myWriter = new FileWriter(filePath);
            myWriter.write(content);
            myWriter.close();
        }
        getLog().info("Total Test Runners Created : "+totalFiles);
    }

    public String ripOffPackageNameFromJavaFile(String content) throws IOException {

        File file = new File(templateRunnerPath);
        String filePath = file.getCanonicalPath();
        filePath = filePath.replaceAll("\\\\",".");
        filePath = StringUtils.substringBetween(filePath,"src.test.java.","."+file.getName());

        if(content.contains("package") && content.contains(filePath)) {
            content = content.replaceAll("package.*"+filePath+".*;","");
        }
        return content;
    }

    public void createDataPropertiesFile() throws IOException {

        File dir = new File(dataDirectoryPath);
        if (!dir.exists()) dir.mkdirs();

        for(int i=0;i<totalFiles;i++) {
            String filePath = dataDirectoryPath + File.separator + fileNamePattern +i+".properties";
            File file = new File(filePath);
            file.createNewFile();
        }
    }

    public void addSpecificsToTestRunnersForTextTemplateFile() throws IOException {

        File folder = new File(runnersDirectoryPath);
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;

        for (File file : listOfFiles) {
            if (file.isFile()) {
                Map<String, String> map = new HashMap<>();
                map.put("TEST_RUNNER_CLASS_NAME",file.getName().split("\\.")[0]);
                map.put("FEATURE_FILES_PATH", featureFilesPath);
                map.put("FEATURE_FILES_TAGS",includedTags);
                replaceTerms(file, map);
            }
        }
    }

    public void addSpecificsToTestRunnersForJavaTemplateFile() throws IOException {

        File javaFile = new File(templateRunnerPath);
        String javaFileName = javaFile.getName().split("\\.")[0];

        File folder = new File(runnersDirectoryPath);
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;

        for (File file : listOfFiles) {
            if (file.isFile()) {
                Map<String, String> map = new HashMap<>();
                map.put(javaFileName,file.getName().split("\\.")[0]);
                map.put("TEST_RUNNER_CLASS_NAME",file.getName().split("\\.")[0]);
                map.put("FEATURE_FILES_PATH", featureFilesPath);
                map.put("FEATURE_FILES_TAGS",includedTags);
                replaceTerms(file, map);
            }
        }
    }

    public void addSpecificsToTestRunners() throws IOException {

        if(templateRunnerPath.contains(".java"))
            addSpecificsToTestRunnersForJavaTemplateFile();
        else
            addSpecificsToTestRunnersForTextTemplateFile();
    }

    public void replaceTerms(File file, Map<String, String> map) throws IOException {

        StringBuilder oldContent = new StringBuilder();
        FileReader fr = new FileReader(file);
        String str;
        try {
            BufferedReader br = new BufferedReader(fr);
            while ((str = br.readLine()) != null) {
                oldContent.append(str).append(System.lineSeparator());
            }
            String content = String.valueOf(oldContent);
            for (Map.Entry<String, String> mapElement : map.entrySet()) {
                String search = mapElement.getKey();
                String replace = mapElement.getValue();
                content = content.replaceAll(search, replace);
            }
            FileWriter writer = new FileWriter(file);
            writer.write(content);
            fr.close();
            writer.close();
        } catch (Exception exc) {
            getLog().info("Exception : "+exc.getMessage());
        }
    }

    public void createPropertiesFilesForEachTestRunner() throws IOException {

        Map<Integer, Map<String, String>> map = configMap;
        File dir = new File(propertiesDirectoryPath);
        if (!dir.exists()) dir.mkdirs();

        for(int i=0;i<totalFiles;i++) {
            String filePath = propertiesDirectoryPath + File.separator + fileNamePattern +i+ ".properties";

            File file = new File(filePath);
            BufferedWriter bf = new BufferedWriter(new FileWriter(file));

            for (Map.Entry<String, String> entry : map.get(i).entrySet()) {
                bf.write(entry.getKey() + ":" + entry.getValue());
                bf.newLine();
            }
            bf.flush();
        }
    }

    public Map<Integer, Map<String, String>> convertXMLToJSONAndReturnMap() throws IOException {

        Map<Integer, Map<String, String>> map = new HashMap<>();
        String content = new String(Files.readAllBytes(Paths.get(configurationFilePath)));
        JSONObject obj = new JSONObject(XML.toJSONObject(content).toString()).getJSONObject("configurations");

        try {
            JSONArray arr = obj.getJSONArray("configuration");
            totalFiles = arr.length();
            for (int i = 0; i < arr.length(); i++) {
                map.put(i, jsonToMap(arr.get(i).toString()));
            }
        }catch(JSONException exc){
            map.put(0, jsonToMap(obj.getJSONObject("configuration").toString()));
            totalFiles = 1;
        }
        return map;
    }

    public Map<String, String> jsonToMap(String content) throws JSONException {

        HashMap<String, String> map = new HashMap<>();
        JSONObject jObject = new JSONObject(content);
        Iterator<?> keys = jObject.keys();

        while( keys.hasNext() ){
            String key = (String)keys.next();
            String value = jObject.get(key).toString();
            map.put(key, value);
        }
        return map;
    }

    public void setSystemVariables(String fileName){

        try {
            File file = new File(propertiesDirectoryPath+File.separator+fileName);
            Properties prop = new Properties();
            InputStream is = new FileInputStream(file);
            prop.load(is);
            for (Map.Entry e : prop.entrySet()){
                System.setProperty(e.getKey().toString(),e.getValue().toString());
            }
            is.close();
        }catch (Exception exc){
            getLog().info(exc.getMessage());
        }
    }

    public void printConfigurationsLogs(){

        getLog().info("------------------------------------------------------------------------");
        getLog().info("Feature Files Path : "+featureFilesPath);
        getLog().info("Tags Included: "+includedTags);
        getLog().info("Config File Path : "+configurationFilePath);
        getLog().info("Runner Template Path : "+templateRunnerPath);
        getLog().info("------------------------------------------------------------------------");
        getLog().info("Running test suite on following configurations : ");
        getLog().info("------------------------------------------------------------------------");

        for (Map.Entry<Integer, Map<String, String>> mapElement : configMap.entrySet()) {
            Map<String, String> map = mapElement.getValue();
            for (Map.Entry<String, String> mapEle : map.entrySet()) {
                getLog().info(mapEle.getKey() + " : " + mapEle.getValue());
            }
            getLog().info("------------------------------------------------------------------------");
        }
    }
}
