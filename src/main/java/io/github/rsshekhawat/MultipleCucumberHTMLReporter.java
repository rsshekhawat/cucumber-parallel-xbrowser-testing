package io.github.rsshekhawat;

import org.apache.commons.text.StringSubstitutor;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import org.apache.maven.plugins.annotations.Parameter;
import org.json.JSONArray;
import org.json.JSONObject;
import static io.github.rsshekhawat.CreateTestRunners.configMap;

@Mojo(name="metadata", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class MultipleCucumberHTMLReporter extends AbstractMojo {

    @Parameter(defaultValue = "${mojoExecution}", readonly = true)
    private MojoExecution mojo;

    @Parameter(property = "metadataFilePath", required = true)
    private String metadataFilePath;

    @Parameter(property = "metadataParameter", required = true)
    private String metadataParameter;

    String cucumberJSONReportsPath = System.getProperty("user.dir") + File.separator + "target" + File.separator + "parallel-xbrowser" + File.separator + "cucumber-report";

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("------------------------------------------------------------------------");
        getLog().info("Plugin Name : " + mojo.getPlugin().getArtifactId());
        getLog().info("Plugin Version : " + mojo.getPlugin().getVersion());
        getLog().info("Developer : Rahul Shekhawat");
        getLog().info("------------------------------------------------------------------------");
        getLog().info("Adding metadata to cucumber json reports...");
        getLog().info("------------------------------------------------------------------------");
        try {
            addMetadataToCucumberJSONFiles();
        } catch (Exception exc) {
            getLog().info(exc.getMessage());
        }
        getLog().info("------------------------------------------------------------------------");
        getLog().info("Successfully added metadata to cucumber json reports. Good Bye !!!");
        getLog().info("------------------------------------------------------------------------");
    }

    public void addMetadataToCucumberJSONFiles() throws IOException {

        File folder = new File(cucumberJSONReportsPath);
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;
        Arrays.sort(listOfFiles);
        int count = 0;

        for (File file : listOfFiles) {
            if (file.isFile()) {
                String filePath = cucumberJSONReportsPath + File.separator + file.getName();
                String content = new String(Files.readAllBytes(Paths.get(filePath)));
                JSONArray arr = new JSONArray(content);

                JSONObject metadata = getMetaData(count);
                for (int i = 0; i < arr.length(); i++) {
                    arr.getJSONObject(i).put("metadata", metadata);
                }
                FileWriter f = new FileWriter(filePath);
                f.write(arr.toString());
                f.close();
                count+=1;
            }
        }
    }

    public JSONObject getMetaData(int num) throws IOException {

        Map<String, String> map = configMap.get(num);
        String metadataJSON = new String(Files.readAllBytes(Paths.get(metadataFilePath)));

        JSONObject jsonObject = new JSONObject(metadataJSON);
        String metadata = jsonObject.get(map.get(metadataParameter)).toString();

        return new JSONObject(new StringSubstitutor(map, "${", "}").replace(metadata));
    }

}