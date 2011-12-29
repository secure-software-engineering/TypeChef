package de.fosd.typechef.lexer.options;

import de.fosd.typechef.featureexpr.FeatureModel;
import de.fosd.typechef.lexer.Warning;
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: kaestner
 * Date: 28.12.11
 * Time: 22:13
 * To change this template use File | Settings | File Templates.
 */
public class LexerOptions extends Options implements  ILexerOptions{
    @Override
    protected List<Option> getOptions() {

        return Arrays.asList(
                new Option[]{
                        new Option("help", LongOpt.NO_ARGUMENT, 'h', null,
                                "Displays help and usage information."),
                        new Option("define", LongOpt.REQUIRED_ARGUMENT, 'D',
                                "name=definition", "Defines the given macro (may currently not be used to define parametric macros)."),
                        new Option("undefine", LongOpt.REQUIRED_ARGUMENT, 'U', "name",
                                "Undefines the given macro, previously either builtin or defined using -D."),
                        new Option(
                                "include",
                                LongOpt.REQUIRED_ARGUMENT,
                                1,
                                "file",
                                "Process file as if \"#"
                                        + "include \"file\"\" appeared as the first line of the primary source file."),
                        new Option("output", LongOpt.REQUIRED_ARGUMENT, 'o', "file",
                                "Output file."),
                        new Option("prefixfilter", LongOpt.REQUIRED_ARGUMENT, 'p', "text",
                                "Analysis excludes all flags beginning with this prefix."),
                        new Option("postfixfilter", LongOpt.REQUIRED_ARGUMENT, 'P', "text",
                                "Analysis excludes all flags ending with this postfix."),
                        new Option("prefixonly", LongOpt.REQUIRED_ARGUMENT, 'x', "text",
                                "Analysis includes only flags beginning with this prefix."),
                        new Option("openFeat", LongOpt.REQUIRED_ARGUMENT, 4, "text",
                                "List of flags with an unspecified value; other flags are considered undefined."),
                        new Option(
                                "incdir",
                                LongOpt.REQUIRED_ARGUMENT,
                                'I',
                                "dir",
                                "Adds the directory dir to the list of directories to be searched for header files."),
                        new Option(
                                "iquote",
                                LongOpt.REQUIRED_ARGUMENT,
                                0,
                                "dir",
                                "Adds the directory dir to the list of directories to be searched for header files included using \"\"."),
                new Option("warning", LongOpt.REQUIRED_ARGUMENT, 'W', "type",
                        "Enables the named warning class (" + getWarningLabels() + ")."),
                new Option("no-warnings", LongOpt.NO_ARGUMENT, 'w', null,
                        "Disables ALL warnings."),
//                new Option("verbose", LongOpt.NO_ARGUMENT, 'v', null,
//                        "Operates incredibly verbosely."),
//                new Option("debug", LongOpt.NO_ARGUMENT, 3, null,
//                        "Operates incredibly verbosely."),
                        new Option("version", LongOpt.NO_ARGUMENT, 2, null,
                                "Prints version number"),});
    }

    private static CharSequence getWarningLabels() {
            StringBuilder buf = new StringBuilder();
            for (Warning w : Warning.values()) {
                if (buf.length() > 0)
                    buf.append(", ");
                String name = w.name().toLowerCase();
                buf.append(name.replace('_', '-'));
            }
            return buf;
        }

    protected Map<String, String> definedMacros = new HashMap<String, String>();
    protected Set<String> undefMacros = new HashSet<String>();
    protected List<String> systemIncludePath = new ArrayList<String>();
    protected List<String> quoteIncludePath = new ArrayList<String>();
    protected List<String> macroFilter = new ArrayList<String>();
    protected List<String> includedHeaders = new ArrayList<String>();
    protected Set<Warning> warnings = new HashSet<Warning>();
    protected String outputName = "";
    protected boolean printVersion = false;

    @Override
    protected boolean interpretOption(int c, Getopt g) throws OptionException {
        switch (c) {
            case 'D':
                //XXX may not be used to define parametric macros
                String arg = g.getOptarg();
                int idx = arg.indexOf('=');
                String name, value;
                if (idx == -1) {
                    name = arg;
                    value = "1";
                } else {
                    name = arg.substring(0, idx);
                    value = arg.substring(idx + 1);
                }
                definedMacros.put(name, value);
                undefMacros.remove(name);
                return true;
            case 'U':
                definedMacros.remove(g.getOptarg());
                undefMacros.add(g.getOptarg());
                return true;
            case 'I':
                // Paths need to be canonicalized, because include_next
                // processing needs to compare paths!
                try {
                    systemIncludePath.add(new File(g.getOptarg()).getCanonicalPath());
                } catch (IOException e) {
                    throw new OptionException("path not found " + g.getOptarg());
                }
                return true;
            case 'p':
                macroFilter.add("p:" + g.getOptarg());
                return true;
            case 'P':
                macroFilter.add("P:" + g.getOptarg());
                return true;
            case 'x':
                macroFilter.add("x:" + g.getOptarg());
                return true;
            case 4:   //--openFeat
                macroFilter.add("4:" + g.getOptarg());
                return true;
            case 0: // --iquote=
                try {
                    quoteIncludePath.add(new File(g.getOptarg()).getCanonicalPath());
                } catch (IOException e) {
                    throw new OptionException("path not found " + g.getOptarg());
                }
                return true;
            case 'W':
                arg = g.getOptarg().toUpperCase();
                arg = arg.replace('-', '_');
                if (arg.equals("ALL"))
                    warnings.addAll(EnumSet.allOf(Warning.class));
                else
                    warnings.add(Enum.valueOf(Warning.class, arg));
                return true;
            case 'w':
                warnings.clear();
                return true;
            case 'o':
                outputName = g.getOptarg();
//                pp.openDebugFiles(outputName);
//                output = new PrintWriter(new BufferedWriter(new FileWriter(outputName)));
                return true;
            case 1: // --include=
                try {
                    includedHeaders.add(new File(g.getOptarg()).getCanonicalPath());
                } catch (IOException e) {
                    throw new OptionException("file not found " + g.getOptarg());
                }
                return true;
            case 2: // --version
                printVersion = true;
                return new ArrayList<Token>();
//            case 'v':
//                pp.addFeature(Feature.VERBOSE);
//                return true;
//            case 3:
//                pp.addFeature(Feature.DEBUG);
//                return true;
            case 'h':
                usage(getClass().getName(), opts);
                return new ArrayList<Token>();
            default:
                return false;
        }

    }

    @Override
    public Map<String, String> getDefinedMacros() {
        return definedMacros;
    }

    @Override
    public Set<String> getUndefMacros() {
        return undefMacros;
    }

    @Override
    public List<String> getSystemIncludePath() {
        return systemIncludePath;
    }

    @Override
    public List<String> getQuoteIncludePath() {
        return quoteIncludePath;
    }

    @Override
    public List<String> getMacroFilter() {
        return macroFilter;
    }

    @Override
    public List<String> getIncludedHeaders() {
        return includedHeaders;
    }

    @Override
    public String getOutputName() {
        return outputName;
    }

    @Override
    public boolean isPrintVersion() {
        return printVersion;
    }

    @Override
    public FeatureModel getFeatureModel() {
        return null;
    }

    @Override
    public Set<Warning> getWarnings() {
        return warnings;
    }
}
