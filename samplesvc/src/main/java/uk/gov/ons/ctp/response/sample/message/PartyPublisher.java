package uk.gov.ons.ctp.response.sample.message;
import uk.gov.ons.ctp.response.party.definition.Party;

/**
 * The publisher to queues
 */
public interface PartyPublisher {
  /**
   * To publish a Party to queue
   * @param partyDTO to be sent
   */
  void publish(Party partyDTO);
}




