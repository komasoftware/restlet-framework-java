package org.restlet.ext.apispark.internal.utils;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.apispark.internal.model.Operation;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.apispark.internal.model.Resource;
import org.restlet.ext.apispark.internal.model.Response;
import org.restlet.ext.apispark.internal.model.Section;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

/**
 * Tools library.
 * 
 * @author Thierry Boileau
 */
public class IntrospectionUtils {

    private static List<String> STRATEGIES = Arrays.asList("add", "reset");


    /**
     * Indicates if the given value is either null or empty.
     *
     * @param value
     *            The value.
     * @return True if the value is either null or empty.
     */
    public static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    /**
     * Displays an option and its description to the console.
     * 
     * @param o
     *            The console stream.
     * @param option
     *            The option.
     * @param strings
     *            The option's description.
     */
    public static void printOption(PrintStream o, String option,
            String... strings) {
        printSentence(o, 7, option);
        printSentence(o, 14, strings);
    }

    /**
     * Formats a list of Strings by lines of 80 characters maximul, and displays
     * it to the console.
     * 
     * @param o
     *            The console.
     * @param shift
     *            The number of characters to shift the list of strings on the
     *            left.
     * @param strings
     *            The list of Strings to display.
     */
    public static void printSentence(PrintStream o, int shift,
            String... strings) {
        int blockLength = 800 - shift - 1;
        String tab = "";
        for (int i = 0; i < shift; i++) {
            tab = tab.concat(" ");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(strings[i]);
        }
        String sentence = sb.toString().replace("\n", "\n" + tab);
        // Cut in slices
        int index = 0;
        while (index < (sentence.length() - 1)) {
            o.print(tab);
            int length = Math.min(index + blockLength, sentence.length() - 1);
            if ((length - index) < blockLength) {
                o.println(sentence.substring(index));
                index = length + 1;
            } else if (sentence.charAt(length) == ' ') {
                o.println(sentence.substring(index, length));
                index = length + 1;
            } else {
                length = sentence.substring(index, length - 1).lastIndexOf(' ');
                if (length != -1) {
                    o.println(sentence.substring(index, index + length));
                    index += length + 1;
                } else {
                    length = sentence.substring(index).indexOf(' ');
                    if (length != -1) {
                        o.println(sentence.substring(index, index + length));
                        index += length + 1;
                    } else {
                        o.println(sentence.substring(index));
                        index = sentence.length();
                    }
                }
            }
        }
    }

    /**
     * Displays a list of String to the console.
     * 
     * @param o
     *            The console stream.
     * @param strings
     *            The list of Strings to display.
     */
    public static void printSentence(PrintStream o, String... strings) {
        printSentence(o, 7, strings);
    }

    /**
     * Displays the command line.
     * 
     * @param o
     *            The console stream.
     * @param clazz
     *            The main class.
     * @param command
     *            The command line.
     */
    public static void printSynopsis(PrintStream o, Class<?> clazz,
            String command) {
        printSentence(o, 7, clazz.getName(), command);
    }

    public static void sendDefinition(Definition definition,
            String descriptorId, String versionId, String ulogin, String upwd,
            String serviceUrl, String updateStrategy, boolean create,
            boolean newVersion, Logger LOGGER) {

        sortDefinition(definition);
        try {
            ClientResource cr = new ClientResource(serviceUrl);
            cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, ulogin, upwd);
            cr.addQueryParameter("type", "rwadef");

            if (create) {
                cr.addSegment("apis").addSegment("");
                LOGGER.info("Create a new descriptor");
                cr.post(definition, MediaType.APPLICATION_JSON);
            } else if (newVersion) {
                cr.addSegment("apis").addSegment(descriptorId)
                        .addSegment("versions").addSegment("");
                LOGGER.info("Create a new version of the descriptor "
                        + descriptorId);
                cr.post(definition, MediaType.APPLICATION_JSON);
            } else if (STRATEGIES.contains(updateStrategy)) {
                cr.addSegment("apis").addSegment(descriptorId)
                        .addSegment("versions").addSegment(versionId)
                        .addSegment("");
                LOGGER.info("Update version " + versionId + " of descriptor "
                        + descriptorId + " with strategy " + updateStrategy);
                cr.addQueryParameter("strategy", updateStrategy);
                if (STRATEGIES.contains(updateStrategy)) {
                    cr.put(definition, MediaType.APPLICATION_JSON);
                } else {
                    LOGGER.severe("The strategy: "
                            + updateStrategy
                            + " is not available. Use parameter --help for help.");
                }
            } else {
                LOGGER.severe("The information you gave is not understandable. Use parameter --help for help.");
            }

            LOGGER.fine("Display result");
            System.out.println("Process successfully achieved.");
            // This is not printed by a logger which may be muted.
            if (cr.getResponseEntity() != null
                    && cr.getResponseEntity().isAvailable()) {
                try {
                    cr.getResponseEntity().write(System.out);
                    System.out.println();
                } catch (IOException e) {
                    // [PENDING] analysis
                    LOGGER.warning("Request successfully achieved by the server, but it's response cannot be printed");
                }
            }
            if (cr.getLocationRef() != null) {
                System.out
                        .println("Your Web API documentation is accessible at this URL: "
                                + cr.getLocationRef());
            }
        } catch (ResourceException e) {
            // TODO Should we detail by status?
            if (e.getStatus().isConnectorError()) {
                LOGGER.severe("Cannot reach the remote service, could you check your network connection?");
                LOGGER.severe("Could you check that the following service is up? "
                        + serviceUrl);
            } else if (e.getStatus().isClientError()) {
                LOGGER.severe("Check that you provide valid credentials, or valid service url.");
            } else if (e.getStatus().isServerError()) {
                LOGGER.severe("The server side encounters some issues, please try later.");
            }
        }
    }

    /**
     * Sorts the sections, representations and resources alphabetically in the
     * given RWADef definition
     * 
     * @param definition
     *            The RWADef definition
     */
    public static void sortDefinition(Definition definition) {
        Collections.sort(definition.getContract().getSections(),
                new Comparator<Section>() {

                    @Override
                    public int compare(Section o1, Section o2) {
                        return o1.getName().compareTo(o2.getName());
                    }

                });
        Collections.sort(definition.getContract().getRepresentations(),
                new Comparator<Representation>() {

                    @Override
                    public int compare(Representation o1, Representation o2) {
                        return o1.getIdentifier().compareTo(o2.getIdentifier());
                    }

                });
        Collections.sort(definition.getContract().getResources(),
                new Comparator<Resource>() {

                    @Override
                    public int compare(Resource o1, Resource o2) {
                        return o1.getResourcePath().compareTo(
                                o2.getResourcePath());
                    }

                });
    }

    public static void updateRepresentationsSectionsFromResources(
            Definition definition) {
        Map<Resource, Collection<String>> resourcesLinks = new HashMap<Resource, Collection<String>>();
        Map<Representation, Collection<String>> representationsSections = new HashMap<Representation, Collection<String>>();
        for (Resource resource : definition.getContract().getResources()) {
            Collection<String> representations = new HashSet<String>();
            for (Operation operation : resource.getOperations()) {
                if (operation.getInputPayLoad() != null
                        && operation.getInputPayLoad().getType() != null) {
                    representations.add(operation.getInputPayLoad().getType());
                }
                for (Response response : operation.getResponses()) {
                    if (response.getOutputPayLoad() != null
                            && response.getOutputPayLoad().getType() != null) {
                        representations.add(response.getOutputPayLoad()
                                .getType());
                    }
                }
            }
            resourcesLinks.put(resource, representations);
        }

        for (Entry<Resource, Collection<String>> entry : resourcesLinks
                .entrySet()) {
            for (String representationIdentifier : entry.getValue()) {
                Representation representation = definition.getContract()
                        .getRepresentation(representationIdentifier);

                //primitives types are not present in representations list
                if (representation != null) {
                    if (representationsSections.get(representation) != null) {
                        representationsSections.get(representation).addAll(
                                entry.getKey().getSections());
                    } else {
                        Collection<String> representationSections = new HashSet<String>();
                        representationSections.addAll(representation.getSections());
                        representationSections.addAll(entry.getKey().getSections());
                        representationsSections.put(representation,
                                representationSections);
                    }
                }
            }
        }

        for (Entry<Representation, Collection<String>> entry : representationsSections
                .entrySet()) {
            entry.getKey().setSections(new ArrayList<String>(entry.getValue()));
        }
    }
}
