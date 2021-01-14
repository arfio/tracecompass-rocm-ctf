/*******************************************************************************
 * Copyright (c) 2014, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.parsers.custom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Custom text wizard tests
 *
 * Some reminders to help making tests (javadoc to keep formatting)
 *
 * Button reminder
 *
 * <pre>
 * 0 Time Stamp Format Help
 * 1 Remove line
 * 2 Add next line
 * 3 Add child line
 * 4 Move up
 * 5 Move down
 * 6 Regular Expression Help
 * 7 Remove group (group 1 toggle)
 * 8 Remove group (group 2 toggle)
 * 9 Add group (group 3 toggle ...)
 * 10 Show parsing result
 * 11 Preview Legend
 * </pre>
 *
 * Combo box reminder
 *
 * <pre>
 * 0 cardinality
 * 1 event type (message, timestamp...)
 * 2 how to handle the data (set, append...)
 * repeat
 * </pre>
 *
 * @author Matthew Khouzam
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class TestCustomTxtWizard extends AbstractCustomParserWizard {

    private static final String MANAGE_CUSTOM_PARSERS_SHELL_TITLE = "Manage Custom Parsers";
    private static final String CUSTOM_TEXT_PARSER_SHELL_TITLE = "Custom Text Parser";
    private static final String PROJECT_NAME = "TestText";
    private static final String CATEGORY_NAME = "Test Category";
    private static final String TRACETYPE_NAME = "Test Trace";
    private static final String EXPECTED_TEST_DEFINITION = "<Definition category=\"Test Category\" name=\"Test Trace\">\n" +
            "<TimeStampOutputFormat>ss</TimeStampOutputFormat>\n" +
            "<InputLine>\n" +
            "<Cardinality max=\"2147483647\" min=\"0\"/>\n" +
            "<RegEx>\\s*(\\d\\d)\\s(.*\\S)</RegEx>\n" +
            "<InputData action=\"0\" format=\"ss\" name=\"Timestamp\" tag=\"TIMESTAMP\"/>\n" +
            "<InputData action=\"0\" name=\"Message\" tag=\"MESSAGE\"/>\n" +
            "</InputLine>\n" +
            "<InputLine>\n" +
            "<Cardinality max=\"2147483647\" min=\"0\"/>\n" +
            "<RegEx>([^0-9]*)</RegEx>\n" +
            "<InputData action=\"2\" name=\"Message\" tag=\"MESSAGE\"/>\n" +
            "</InputLine>\n" +
            "<OutputColumn name=\"Timestamp\" tag=\"TIMESTAMP\"/>\n" +
            "<OutputColumn name=\"Message\" tag=\"MESSAGE\"/>\n";

    /**
     * Test to create a custom txt trace and compare the xml
     *
     * @throws IOException
     *             the xml file is not accessible, this is bad
     * @throws FileNotFoundException
     *             the xml file wasn't written, this is bad
     */
    @Test
    public void testNew() throws FileNotFoundException, IOException {
        File xmlFile = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(".metadata/.plugins/org.eclipse.tracecompass.tmf.core/custom_txt_parsers.xml").toFile();
        // Open the custom parsers dialog
        SWTBotUtils.createProject(PROJECT_NAME);

        SWTBotTreeItem treeNode = SWTBotUtils.selectTracesFolder(fBot, PROJECT_NAME);
        treeNode.contextMenu("Manage Custom Parsers...").click();
        SWTBotShell shell = fBot.shell(MANAGE_CUSTOM_PARSERS_SHELL_TITLE).activate();
        shell.setFocus();
        SWTBot bot = shell.bot();

        // Open the new custom txt parser dialog
        bot.button("New...").click();
        shell = bot.shell(CUSTOM_TEXT_PARSER_SHELL_TITLE).activate();
        shell.setFocus();
        bot = shell.bot();

        // Setting header
        bot.textWithLabel("Category:").setText(CATEGORY_NAME);
        bot.textWithLabel("Trace type:").setText(TRACETYPE_NAME);
        bot.textWithLabel("Time Stamp format:").setText("ss");

        // Fill Group 1 as time stamp
        bot.comboBox(1).setSelection("Timestamp");
        bot.textWithLabel("format:").setText("ss");
        // Click on the New group button
        bot.button(8).click();
        // Add next line
        bot.button(2).click();
        SWTBotTreeItem[] treeItems = bot.tree().getAllItems();
        SWTBotTreeItem eventLine[] = new SWTBotTreeItem[2];
        treeItems = bot.tree().getAllItems();
        for (SWTBotTreeItem item : treeItems) {
            if (item.getText().startsWith("Root Line 1")) {
                eventLine[0] = item;
            }
            if (item.getText().startsWith("Root Line 2")) {
                eventLine[1] = item;
            }
        }
        assertNotNull(eventLine[0]);
        assertNotNull(eventLine[1]);
        // Set the regular expression for each event line
        bot.styledText().setText("12 Hello\nWorld\n23 Goodbye\ncruel world");
        eventLine[0].select();
        WaitUtils.waitForJobs();
        bot.textWithLabel("Regular expression:").setText("\\s*(\\d\\d)\\s(.*\\S)");
        eventLine[1].select();
        bot.textWithLabel("Regular expression:").setText("([^0-9]*)");
        // Click on the new group of root line 2
        bot.button(7).click();
        bot.comboBox("Set").setSelection("Append with |");
        bot.button("Highlight All").click();
        bot.button("Next >").click();
        bot.button("Finish").click();

        shell = bot.shell(MANAGE_CUSTOM_PARSERS_SHELL_TITLE).activate();
        shell.setFocus();
        bot = shell.bot();
        fBot.waitUntil(new CustomDefinitionHasContent(xmlFile, CATEGORY_NAME, TRACETYPE_NAME, EXPECTED_TEST_DEFINITION));
        String xmlPart = extractTestXml(xmlFile, CATEGORY_NAME, TRACETYPE_NAME);
        assertEquals(EXPECTED_TEST_DEFINITION, xmlPart);
        bot.list().select(CATEGORY_NAME + " : " + TRACETYPE_NAME);
        bot.button("Delete").click();

        shell = fBot.shell("Delete Custom Parser").activate();
        shell.setFocus();
        bot = shell.bot();
        bot.button("Yes").click();

        shell = fBot.shell(MANAGE_CUSTOM_PARSERS_SHELL_TITLE).activate();
        shell.setFocus();
        bot = shell.bot();
        bot.button("Close").click();
        fBot.waitUntil(new CustomDefinitionHasContent(xmlFile, CATEGORY_NAME, TRACETYPE_NAME, ""));
        xmlPart = extractTestXml(xmlFile, CATEGORY_NAME, TRACETYPE_NAME);
        assertEquals("", xmlPart);

        SWTBotUtils.deleteProject(PROJECT_NAME, fBot);
    }

    /**
     * Test to edit a custom txt trace and compare the xml
     *
     * @throws IOException
     *             the xml file is not accessible, this is bad
     * @throws FileNotFoundException
     *             the xml file wasn't written, this is bad
     */
    @Test
    public void testEdit() throws FileNotFoundException, IOException {
        File xmlFile = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(".metadata/.plugins/org.eclipse.tracecompass.tmf.core/custom_txt_parsers.xml").toFile();
        try (FileWriter fw = new FileWriter(xmlFile)) {
            String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<CustomTxtTraceDefinitionList>\n" +
                    "<Definition category=\"Demo Category\" name=\"Demo trace\">\n" +
                    "<TimeStampOutputFormat>sss</TimeStampOutputFormat>\n" +
                    "<InputLine>\n" +
                    "<Cardinality max=\"2147483647\" min=\"0\"/>\n" +
                    "<RegEx>\\s*(\\d*)\\s(.*)</RegEx>\n" +
                    "<InputData action=\"0\" format=\"sss\" name=\"Timestamp\" tag=\"TIMESTAMP\"/>\n" +
                    "<InputData action=\"0\" name=\"Message\" tag=\"MESSAGE\"/>\n" +
                    "</InputLine>\n" +
                    "<OutputColumn name=\"Timestamp\" tag=\"TIMESTAMP\"/>\n" +
                    "<OutputColumn name=\"Message\" tag=\"MESSAGE\"/>\n" +
                    "</Definition>\n" +
                    "<Definition name=\"dmesg\">\n" +
                    "<TimeStampOutputFormat>sssssss.ssssss</TimeStampOutputFormat>\n" +
                    "<InputLine>\n" +
                    "<Cardinality max=\"2147483647\" min=\"0\"/>\n" +
                    "<RegEx>^[([0-9]*\\.[0.9]*)]\\s(.*)</RegEx>\n" +
                    "<InputData action=\"0\" format=\"sssss.sssss\" name=\"Timestamp\" tag=\"TIMESTAMP\"/>\n" +
                    "<InputData action=\"0\" name=\"Message\" tag=\"MESSAGE\"/>\n" +
                    "</InputLine>\n" +
                    "<OutputColumn name=\"Timestamp\" tag=\"TIMESTAMP\"/>\n" +
                    "<OutputColumn name=\"Message\" tag=\"MESSAGE\"/>\n" +
                    "</Definition>\n" +
                    "</CustomTxtTraceDefinitionList>";
            fw.write(xmlContent);
            fw.flush();
        }
        // Open the custom parsers dialog
        SWTBotUtils.createProject(PROJECT_NAME);
        SWTBotTreeItem treeNode = SWTBotUtils.selectTracesFolder(fBot, PROJECT_NAME);
        treeNode.contextMenu("Manage Custom Parsers...").click();
        SWTBotShell shell = fBot.shell(MANAGE_CUSTOM_PARSERS_SHELL_TITLE).activate();
        shell.setFocus();
        SWTBot bot = shell.bot();
        // Open the edition dialog for txt parser
        bot.list().select("Demo Category : Demo trace");
        bot.button("Edit...").click();

        shell = fBot.shell(CUSTOM_TEXT_PARSER_SHELL_TITLE).activate();
        shell.setFocus();
        bot = shell.bot();

        // update parser's data
        bot.textWithLabel("Category:").setText(CATEGORY_NAME);
        bot.textWithLabel("Trace type:").setText(TRACETYPE_NAME);
        bot.textWithLabel("Time Stamp format:").setText("ss");

        // update time stamp format
        bot.comboBox(1).setSelection("Timestamp");
        bot.textWithLabel("format:").setText("ss");
        bot.button(2).click();
        SWTBotTreeItem[] treeItems = fBot.tree().getAllItems();
        SWTBotTreeItem eventLine[] = new SWTBotTreeItem[2];
        for (SWTBotTreeItem item : treeItems) {
            if (item.getText().startsWith("Root Line 1")) {
                eventLine[0] = item;
            }
            if (item.getText().startsWith("Root Line 2")) {
                eventLine[1] = item;
            }
        }
        treeItems = fBot.tree().getAllItems();
        assertNotNull(eventLine[0]);
        assertNotNull(eventLine[1]);
        bot.styledText().setText("12 Hello\nWorld\n23 Goodbye\ncruel world");
        eventLine[0].select();
        WaitUtils.waitForJobs();
        bot.textWithLabel("Regular expression:").setText("\\s*(\\d\\d)\\s(.*\\S)");
        eventLine[1].select();
        bot.textWithLabel("Regular expression:").setText("([^0-9]*)");
        bot.button(7).click();
        bot.comboBox("Set").setSelection("Append with |");
        bot.button("Highlight All").click();
        bot.button("Next >").click();
        bot.button("Finish").click();

        shell = bot.shell(MANAGE_CUSTOM_PARSERS_SHELL_TITLE).activate();
        shell.setFocus();
        bot = shell.bot();

        fBot.waitUntil(new CustomDefinitionHasContent(xmlFile, CATEGORY_NAME, TRACETYPE_NAME, EXPECTED_TEST_DEFINITION));
        String xmlPart = extractTestXml(xmlFile, CATEGORY_NAME, TRACETYPE_NAME);
        assertEquals(EXPECTED_TEST_DEFINITION, xmlPart);
        bot.list().select(CATEGORY_NAME + " : " + TRACETYPE_NAME);
        bot.button("Delete").click();

        shell = fBot.shell("Delete Custom Parser").activate();
        shell.setFocus();
        bot = shell.bot();
        bot.button("Yes").click();

        shell = fBot.shell(MANAGE_CUSTOM_PARSERS_SHELL_TITLE).activate();
        shell.setFocus();
        bot = shell.bot();
        bot.button("Close").click();

        fBot.waitUntil(new CustomDefinitionHasContent(xmlFile, CATEGORY_NAME, TRACETYPE_NAME, ""));
        xmlPart = extractTestXml(xmlFile, CATEGORY_NAME, TRACETYPE_NAME);
        assertEquals("", xmlPart);

        SWTBotUtils.deleteProject(PROJECT_NAME, fBot);
    }
}
