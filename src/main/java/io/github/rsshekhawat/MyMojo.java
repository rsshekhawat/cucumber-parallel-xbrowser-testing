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
import org.json.XML;

@Mojo(name="xbrowser", defaultPhase = LifecyclePhase.INITIALIZE)
public class MyMojo extends AbstractMojo
{
    @Parameter(property = "project", readonly = true)
    private MavenProject mavenProject;

    @Parameter(property = "templateRunnerPath", required = true)
    private String templateRunnerPath;

    @Parameter(property = "featureFilesPath", required = true)
    private String featureFilesPath;

    @Parameter(property = "configurationFilePath", required = true)
    private String configurationFilePath;

    @Parameter(property = "includedTags", required = true)
    private String includedTags;

    @Parameter( defaultValue = "${mojoExecution}", readonly = true )
    private MojoExecution mojo;

    String runnersDirectoryPath = System.getProperty("user.dir")+ File.separator+"target"+File.separator+"parallel-xbrowser"+File.separator+"runners";
    Map<Integer, Map<String, String>> configMap;

    @Override
    public void execute() throws MojoExecutionException {

        getLog().info("------------------------------------------------------------------------");
        getLog().info("Plugin Name : "+mojo.getPlugin().getArtifactId());
        getLog().info("Plugin Version : "+mojo.getPlugin().getVersion());
        getLog().info("Developer : Rahul Shekhawat");
        getLog().info("------------------------------------------------------------------------");
        try {
            configMap = convertXMLToJSONAndReturnMap();
            prepareTemplateRunnerFile();
            createTestRunners();
            addSpecificsToTestRunners();
            printConfigurationsLogs();
        } catch (Exception exc) {
            getLog().info("Exception : "+exc.getMessage());
        }
    }

    public void prepareTemplateRunnerFile() throws IOException {

        Map<String, String> map = new HashMap<>();
        map.put("FEATURE_FILES_PATH", featureFilesPath);
        map.put("FEATURE_FILES_TAGS",includedTags);

        String filePath = templateRunnerPath;
        File file = new File(filePath);
        replaceTerms(file, map);
    }

    public void createTestRunners() throws IOException {

        int totalFiles = configMap.size();
        String content = new String(Files.readAllBytes(Paths.get(templateRunnerPath)));
        String fileName = "IT_Cucumber_Parallel_Cross_Browser_Test_Runner_";
        File dir = new File(runnersDirectoryPath);

        for(int i=0;i<totalFiles;i++) {
            String filePath = runnersDirectoryPath + File.separator + fileName+i+".java";
            if (!dir.exists()) dir.mkdirs();
            File file = new File(filePath);
            boolean flag = file.createNewFile();
            FileWriter myWriter = new FileWriter(filePath);
            myWriter.write(content);
            myWriter.close();
        }
        getLog().info("Total Test Runners Created : "+totalFiles);
    }

    public void addSpecificsToTestRunners() throws IOException {

        File folder = new File(runnersDirectoryPath);
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;

        int index = 0;

        for (File file : listOfFiles) {
            if (file.isFile()) {
                Map<String, String> m = configMap.get(index);
                m.put("TEST_RUNNER_CLASS_NAME",file.getName().split("\\.")[0]);
                replaceTerms(file, m);
                m.remove("TEST_RUNNER_CLASS_NAME");
                index+=1;
            }
        }
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

    public Map<Integer, Map<String, String>> convertXMLToJSONAndReturnMap() throws IOException {

        Map<Integer, Map<String, String>> map = new HashMap<>();
        String content = new String(Files.readAllBytes(Paths.get(configurationFilePath)));
        JSONObject obj = new JSONObject(XML.toJSONObject(content).toString()).getJSONObject("configurations");
        JSONArray arr = obj.getJSONArray("configuration");
        for(int i=0;i<arr.length();i++){
            map.put(i, jsonToMap(arr.get(i).toString()));
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
            map.put(key+"Value", value);
        }
        return map;
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
                getLog().info(StringUtils.substringBefore(mapEle.getKey(),"Value") + " : " + mapEle.getValue());
            }
            getLog().info("------------------------------------------------------------------------");
        }
    }
}
