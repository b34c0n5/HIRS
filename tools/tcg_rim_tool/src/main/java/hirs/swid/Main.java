package hirs.swid;

import com.beust.jcommander.JCommander;
import hirs.swid.utils.Commander;
import hirs.swid.utils.CredentialArgumentValidator;
import hirs.swid.utils.TimestampArgumentValidator;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        Commander commander = new Commander();
        JCommander jc = JCommander.newBuilder().addObject(commander).build();
        jc.parse(args);
        SwidTagGateway gateway;
        SwidTagValidator validator;
        CredentialArgumentValidator caValidator;
        String rimEventLogFile, trustStoreFile, certificateFile, privateKeyFile;

        if (commander.isHelp()) {
            jc.usage();
            System.out.println(commander.printHelpExamples());
        } else {
            if (!commander.getVerifyFile().isEmpty()) {
                validator = new SwidTagValidator();
                System.out.println(commander.toString());
                String verifyFile = commander.getVerifyFile();
                certificateFile = commander.getPublicCertificate();
                rimEventLogFile = commander.getRimEventLog();
                trustStoreFile = commander.getTruststoreFile();
                boolean defaultKey = commander.isDefaultKey();
                if (defaultKey) {
                    validator.validateSwidTag(verifyFile, "DEFAULT");
                } else {
                    caValidator = new CredentialArgumentValidator(trustStoreFile,
                            certificateFile, "", "", "", true);
                    if (caValidator.isValid()) {
                        validator.setTrustStoreFile(trustStoreFile);
                        validator.validateSwidTag(verifyFile, caValidator.getFormat());
                    } else {
                        System.out.println("Invalid combination of credentials given: "
                                + caValidator.getErrorMessage());
                        System.exit(1);
                    }
                }
            } else {
                gateway = new SwidTagGateway();
                System.out.println(commander.toString());
                rimEventLogFile = commander.getRimEventLog();
                trustStoreFile = commander.getTruststoreFile();
                certificateFile = commander.getPublicCertificate();
                privateKeyFile = commander.getPrivateKeyFile();
                boolean embeddedCert = commander.isEmbedded();
                boolean defaultKey = commander.isDefaultKey();
                if (!commander.getSignFile().isEmpty()) {

                } else {
                    String createType = commander.getCreateType().toUpperCase();
                    String attributesFile = commander.getAttributesFile();
                    if (createType.equals("BASE")) {
                        if (!attributesFile.isEmpty()) {
                            gateway.setAttributesFile(attributesFile);
                        }
                        if (defaultKey) {
                            gateway.setDefaultCredentials(true);
                            gateway.setTruststoreFile(SwidTagConstants.DEFAULT_KEYSTORE_FILE);
                        } else {
                            gateway.setDefaultCredentials(false);
                            caValidator = new CredentialArgumentValidator(trustStoreFile,
                                    certificateFile, privateKeyFile, "", "", false);
                            if (caValidator.isValid()) {
                                gateway.setTruststoreFile(trustStoreFile);
                                gateway.setPemCertificateFile(certificateFile);
                                gateway.setPemPrivateKeyFile(privateKeyFile);
                            } else {
                                System.out.println("Invalid combination of credentials given: "
                                        + caValidator.getErrorMessage());
                                System.exit(1);
                            }
                            if (embeddedCert) {
                                gateway.setEmbeddedCert(true);
                            }
                        }
                        gateway.setRimEventLog(rimEventLogFile);
                        List<String> timestampArguments = commander.getTimestampArguments();
                        if (timestampArguments.size() > 0) {
                            if (new TimestampArgumentValidator(timestampArguments).isValid()) {
                                gateway.setTimestampFormat(timestampArguments.get(0));
                                if (timestampArguments.size() > 1) {
                                    gateway.setTimestampArgument(timestampArguments.get(1));
                                }
                            } else {
                                System.exit(1);
                            }
                        }
                        gateway.generateSwidTag(commander.getOutFile());
                    } else {
                        System.out.println("No create type given, nothing to do");
                        System.exit(1);
                    }
                }
                if (!trustStoreFile.isEmpty()) {
                    gateway.setDefaultCredentials(true);
                    gateway.setJksTruststoreFile(trustStoreFile);
                } else if (!certificateFile.isEmpty() && !privateKeyFile.isEmpty()) {
                    gateway.setDefaultCredentials(false);
                    gateway.setPemCertificateFile(certificateFile);
                    gateway.setPemPrivateKeyFile(privateKeyFile);
                    if (embeddedCert) {
                        gateway.setEmbeddedCert(true);
                    }
                } else if (defaultKey) {
                    gateway.setDefaultCredentials(true);
                    gateway.setJksTruststoreFile(SwidTagConstants.DEFAULT_KEYSTORE_FILE);
                } else {
                    System.out.println("A private key (-k) and public certificate (-p) " +
                            "are required, or the default key (-d) must be indicated.");
                    System.exit(1);
                }
                gateway.generateSwidTag(commander.getOutFile());
            }
        }
    }
}
