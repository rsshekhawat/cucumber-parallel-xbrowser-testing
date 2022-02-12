package io.github.rsshekhawat;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;
import net.masterthought.cucumber.Reportable;
import net.masterthought.cucumber.json.support.Status;
import net.masterthought.cucumber.presentation.PresentationMode;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name="xbrowser-reports", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class CustomCucumberReports extends AbstractMojo {

    @Parameter( defaultValue = "${mojoExecution}", readonly = true )
    private MojoExecution mojo;

    @Parameter( property = "Qualifiers", required = true)
    private String Qualifiers;

    String propertiesDirectoryPath = System.getProperty("user.dir")+ File.separator+"target"+File.separator+"parallel-xbrowser"+File.separator+"properties";

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("------------------------------------------------------------------------");
        getLog().info("Plugin Name : "+mojo.getPlugin().getArtifactId());
        getLog().info("Plugin Version : "+mojo.getPlugin().getVersion());
        getLog().info("Developer : Rahul Shekhawat");
        getLog().info("------------------------------------------------------------------------");
        getLog().info("Creating customized cucumber reports...");
        getLog().info("------------------------------------------------------------------------");
        createCucumberReports();
        getLog().info("------------------------------------------------------------------------");
        getLog().info("Successfully generated customized cucumber reports. Good Bye !!!");
        getLog().info("------------------------------------------------------------------------");
    }

    public void createCucumberReports() {

        File reportOutputDirectory = new File("target");
        List<String> jsonFiles = new ArrayList<>();
        String cucumberReportDirectory = System.getProperty("user.dir")+File.separator+"target"+File.separator+"parallel-xbrowser"+File.separator+"cucumber-report";
        File folder = new File(cucumberReportDirectory);
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;
        for (File file : listOfFiles) {
            if (file.isFile()) {
                jsonFiles.add(file.getAbsolutePath());
            }
        }

        String buildNumber = System.getenv("BUILD_NUMBER");
        String projectName = System.getenv("JOB_BASE_NAME");

        Configuration configuration = new Configuration(reportOutputDirectory, projectName);

        // optional configuration - check javadoc for details
        configuration.addPresentationModes(PresentationMode.RUN_WITH_JENKINS);

        // do not make scenario failed when step has status SKIPPED
        configuration.setNotFailingStatuses(Collections.singleton(Status.SKIPPED));
        configuration.setBuildNumber(buildNumber);

        // optionally specify qualifiers for each of the report json files
        configuration.addPresentationModes(PresentationMode.PARALLEL_TESTING);

        int count = 0;
        for (Map.Entry<String, String> mapElement : getQualifierMap(Qualifiers).entrySet()) {
            count+=1;
            configuration.addClassifications("Target "+count, mapElement.getValue());
            configuration.setQualifier(mapElement.getKey(), mapElement.getValue());
        }

        ReportBuilder reportBuilder = new ReportBuilder(jsonFiles, configuration);
        Reportable result = reportBuilder.generateReports();

        // and here validate 'result' to decide what to do if report has failed
    }

    public Map<String, String> getQualifierMap(String list){

        Map<String, String> map = new HashMap<>();
        File folder = new File(propertiesDirectoryPath);
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;
        for (File file : listOfFiles) {
            if (file.isFile()) {
                String key = file.getName().split("\\.")[0];
                String value = getQualifierValue(file.getName(),list);
                map.put(key, value);
            }
        }
        return map;
    }

    public String getQualifierValue(String fileName, String list){

        List<String> res = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        try {
            File file = new File(propertiesDirectoryPath+File.separator+fileName);
            Properties prop = new Properties();
            InputStream is = new FileInputStream(file);
            prop.load(is);
            for (String key : prop.stringPropertyNames()){
                String value = prop.getProperty(key);
                map.put(key, value);
            }
            is.close();
            String[] arr = list.split("\\s+");

            for(String item: arr){
                if(map.get(item)==null)
                    continue;
                res.add(map.get(item));
            }

        }catch (Exception exc){
            getLog().info("Exception : "+exc.getMessage());
        }
        return String.join(", ", res);
    }
}
