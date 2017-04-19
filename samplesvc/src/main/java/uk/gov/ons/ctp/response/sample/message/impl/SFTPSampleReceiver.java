package uk.gov.ons.ctp.response.sample.message.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageBuilder;

/**
 * Created by Kieran Wardle on 13/04/2017.
 */
@Slf4j
@MessageEndpoint
public class SFTPSampleReceiver {

    /**
     * Confirms file rename successful for XML input file
     * @param message success message
     */
    @ServiceActivator(inputChannel = "renameSuccessProcess")
    public void sftpSuccessProcess(GenericMessage<GenericMessage<byte[]>> message) {
        String filename = (String) message.getPayload().getHeaders().get("file_name");
        log.debug("Renaming successful for " + filename);
    }

    /**
     * Confirms file rename unsuccessful for XML input file
     * @param message failure message
     */
    @ServiceActivator(inputChannel = "renameFailedProcess")
    public void sftpFailedProcess(GenericMessage<MessagingException> message) {
        String filename = (String) message.getPayload().getFailedMessage().getHeaders().get("file_name");
        log.debug("Renaming failed for" + filename);
    }

    /**
     * Creates error file containing the reason for XML validation failure
     * @param errorMessage failure message containing reason for failure
     * @return Message<String> message containing cut down error message and new file names
     */
    @ServiceActivator(inputChannel = "pollerErrorChannel", outputChannel = "errorUploadChannel")
    public Message<String> processInvalidSample(GenericMessage errorMessage) {

        String error = ((Exception) errorMessage.getPayload()).getCause().toString();
        log.debug(error);
        String fileName = ((MessagingException) errorMessage.getPayload()).getFailedMessage().getHeaders()
                .get("file_name").toString();
        String directory = ((MessagingException) errorMessage.getPayload()).getFailedMessage().getHeaders()
                .get("sample_type").toString() + "-sftp";
        String shortFileName = fileName.replace(".xml", "");
        String errorFile = shortFileName + "_error.txt";

        log.debug(fileName + " Was invalid and rejected.");

        final Message<String> message = MessageBuilder.withPayload(error).setHeader("error_file_name",
                errorFile).setHeader("file_name", fileName).setHeader("short_file_name",
                shortFileName).setHeader("directory", directory).build();

        return message;
    }
}
