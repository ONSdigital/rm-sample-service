package uk.gov.ons.ctp.response.sample.message;
import uk.gov.ons.ctp.response.party.definition.Party;

/**
 * The publisher to queues
 */
public interface SendToParty {
  /**
   * To publish a caseReceipt to queue
   * @param partyDTO to be sent
   */
  void send(Party partyDTO);
}




