package de.westemeyer.plugins.multiselect;

import hudson.EnvVars;
import hudson.model.ParameterValue;
import hudson.util.FormValidation;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static de.westemeyer.plugins.multiselect.MultiselectConfigurationFormat.CSV;

public class MultiselectParameterDefinitionTest {
    private static final String INPUT_STRING = "H,Type,Sport,Country,Team\nV,SELECTED_TYPE,SELECTED_SPORT,SELECTED_COUNTRY,SELECTED_TEAM\nC,Water,Wakeboarding,Germany,WSC Duisburg Rheinhausen\nC,Water,Wakeboarding,Germany,WSC Paderborn\nC,Water,Wakeboarding,Austria,WSC Wien\nT,,,,Alternative team name\nC,Water,Waterball,Germany,Waterball Team\nC,Water,Surfing,England,Bristol Surf Team\nC,Ball,Football,France,Paris St. Germain\nT,,,,Alternative team name\nC,Ball,Handball,Germany,THW Kiel\n";
    private static final MultiselectDecisionTree INPUT = MultiselectDecisionTree.parse(INPUT_STRING);
    private static final String VALIDATION1_STRING = "H,Type,Sport,Country,Team\nV,SELECTED_TYPE,SELECTED_SPORT,SELECTED_COUNTRY,SELECTED_TEAM\nC,Water,Wakeboarding,Germany,WSC Duisburg Rheinhausen,hello\nC,Water,Wakeboarding,Germany,WSC Paderborn\nC,Water,Wakeboarding,Austria,WSC Wien\nT,,,,Alternative team name\nC,Water,Waterball,Germany,Waterball Team\nC,Water,Surfing,England,Bristol Surf Team\nC,Ball,Football,France,Paris St. Germain\nT,,,,Alternative team name\nC,Ball,Handball,Germany,THW Kiel\n";
    private static final MultiselectDecisionTree VALIDATION1 = MultiselectDecisionTree.parse(VALIDATION1_STRING);

    @Test
    void constructor() {
        MultiselectParameterDefinition definition = new MultiselectParameterDefinition("name", "description", INPUT, CSV);
        Assert.assertEquals(CSV, definition.getFormat());
        Assert.assertEquals(INPUT, definition.getDecisionTree());
        definition.setDecisionTree(VALIDATION1);
        Assert.assertEquals(VALIDATION1, definition.getDecisionTree());
        // Currently there is only just one supported format. Add different value later, assert format change.
        definition.setFormat(CSV);
    }

    @Test
    void getItemList() {
        MultiselectParameterDefinition definition = new MultiselectParameterDefinition("name", "description", INPUT, CSV);
        Integer[] coordinates = new Integer[] {0, 0};
        String[] itemList = definition.getItemList(coordinates);
        Assert.assertEquals(2, itemList.length);
        Assert.assertEquals("Germany", itemList[0]);
        Assert.assertEquals("Austria", itemList[1]);
    }

    @Test
    void getDefaultParameterValue() {
        MultiselectParameterDefinition definition = new MultiselectParameterDefinition("name", "description", INPUT, CSV);
        ParameterValue defaultParameterValue = definition.getDefaultParameterValue();
        Assert.assertNotNull(defaultParameterValue);
        Assert.assertEquals("name", defaultParameterValue.getName());
        Object value = defaultParameterValue.getValue();
        Assert.assertNotNull(value);
        Assert.assertEquals("{}", value.toString());
    }

    @Test
    void createValue() {
        MultiselectParameterDefinition definition = new MultiselectParameterDefinition("name", "description", INPUT, CSV);
        ParameterValue defaultParameterValue = definition.createValue((StaplerRequest) null);
        Assert.assertNotNull(defaultParameterValue);
        Assert.assertEquals("name", defaultParameterValue.getName());
        Object parameterValueContent = defaultParameterValue.getValue();
        Assert.assertNotNull(parameterValueContent);
        Assert.assertEquals("{}", parameterValueContent.toString());

        Map<String, Object> values = new HashMap<>();
        values.put("SELECTED_TYPE", "0");
        values.put("SELECTED_SPORT", "1");
        values.put("SELECTED_COUNTRY", "0");
        values.put("SELECTED_TEAM", "0");
        MultiselectParameterValue value = definition.createValue(values);
        EnvVars vars = new EnvVars();
        value.buildEnvironment(null, vars);
        Assert.assertEquals("Water", vars.get("SELECTED_TYPE"));
        Assert.assertEquals("Waterball", vars.get("SELECTED_SPORT"));
        Assert.assertEquals("Germany", vars.get("SELECTED_COUNTRY"));
        Assert.assertEquals("Waterball Team", vars.get("SELECTED_TEAM"));
    }

    @Test
    void doCheckConfiguration() throws IOException {
        MultiselectParameterDefinition.DescriptorImpl descriptor = new MultiselectParameterDefinition.DescriptorImpl();
        FormValidation formValidation = descriptor.doCheckConfiguration(INPUT_STRING);
        Assert.assertEquals(FormValidation.Kind.OK, formValidation.kind);
        Assert.assertNull(formValidation.getMessage());
        formValidation = descriptor.doCheckConfiguration(VALIDATION1_STRING);
        Assert.assertEquals(Messages.FormValidation_NotEnoughColumns(3), formValidation.getMessage());
        Assert.assertEquals(FormValidation.Kind.WARNING, formValidation.kind);
        formValidation = descriptor.doCheckConfiguration("");
        Assert.assertEquals(Messages.FormValidation_ConfigurationIsEmpty(), formValidation.getMessage());
        Assert.assertEquals(FormValidation.Kind.ERROR, formValidation.kind);
    }

    @Test
    void getDisplayName() {
        MultiselectParameterDefinition.DescriptorImpl descriptor = new MultiselectParameterDefinition.DescriptorImpl();
        Assert.assertEquals(Messages.MultiselectParameterDefinition_DisplayName(), descriptor.getDisplayName());
    }

    @Test
    void getUuid() {
        MultiselectParameterDefinition definition = new MultiselectParameterDefinition("name", "description", null, CSV);
        Assert.assertNotNull(definition.getUuid());
        Assert.assertEquals(15, definition.getUuid().length());
    }

    @Test
    void newInstance() {
        MultiselectParameterDefinition.DescriptorImpl descriptor = new MultiselectParameterDefinition.DescriptorImpl();
        MultiselectParameterDefinition definition = descriptor.newInstance(INPUT_STRING, "parametername", "description");
        Assert.assertEquals(INPUT_STRING, definition.getDecisionTree().toString());
    }
}
