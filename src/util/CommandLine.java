package util;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandLine extends Option {
    private static final Pattern optionPattern = Pattern.compile("\\[-(\\w+)((?: \\w+)*)\\]");
    private final Map<String, Option> possibleOptions;

    public CommandLine(String optionDescriptor, String[] argv) throws CommandLineParseException {
        int optionEndIndex = 0;
        Matcher matcher = optionPattern.matcher(optionDescriptor);
        Map <String, Option> possibleOptions = new ConcurrentHashMap<String, Option>();
        while (matcher.find()) {
            Option option = new Option();
            option.parseArgumentDescriptor(matcher.group(2));
            possibleOptions.put(matcher.group(1), option);
            optionEndIndex = matcher.end();
        }

        String remainder = optionDescriptor.substring(optionEndIndex);
        parseArgumentDescriptor(remainder);
        Option currentOption = this;
        for (String arg : argv) {
            if (currentOption != this && !currentOption.needsMoreArguments())
                currentOption = this;
            if (arg.startsWith("-")) {
                if (currentOption.needsMoreArguments() && currentOption != this) {
                    throw new CommandLineParseException(currentOption + " requires an argument");
                } else {
                    String argName = arg.substring(1);
                    currentOption = possibleOptions.get(argName);
                    if (currentOption != null) {
                        currentOption.active = true;
                    } else {
                        throw new CommandLineParseException("Invalid argument: " + arg);
                    }
                }
            } else if (currentOption.needsMoreArguments()) {
                currentOption.addArgument(arg);
            } else {
                throw new CommandLineParseException("Too many arguments");
            }
        }
        if (currentOption.needsMoreArguments())
            throw new CommandLineParseException(currentOption + " requires an argument");

        this.possibleOptions = Collections.synchronizedMap(possibleOptions);
    }

    public boolean hasOption(String optionName) {
        Option option = possibleOptions.get(optionName);
        return option != null && option.active;

    }

    public String getOptionArgument(String optionName, String argName) {
        Option option = possibleOptions.get(optionName);
        if (option == null)
            return null;
        else
            return option.getArgument(argName);
    }

}

