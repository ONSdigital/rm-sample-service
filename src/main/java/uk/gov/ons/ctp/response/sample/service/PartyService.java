package uk.gov.ons.ctp.response.sample.service;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import libs.party.representation.PartyDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ctp.response.party.definition.PartyCreationRequestDTO;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;

@Service
public class PartyService {

  private static final Logger LOG = LoggerFactory.getLogger(PartyService.class);

  @Autowired private PartySvcClientService partySvcClient;

  @Autowired SampleUnitRepository sampleUnitRepository;

  @Async
  public CompletableFuture<Void> sendToPartyService(
      String sampleUnitId, PartyCreationRequestDTO partyCreationRequest) {
    return CompletableFuture.runAsync(
        () -> {
          try {
            LOG.debug("about to send request to party service", kv("sampleUnitId", sampleUnitId));
            PartyDTO party = partySvcClient.postParty(partyCreationRequest);
            LOG.debug(
                "party creation successful",
                kv("sampleUnitId", sampleUnitId),
                kv("partyId", party.getId()));

            addPartyIdToSample(sampleUnitId, party);

          } catch (DataAccessException exc) {
            LOG.error("unable to save party id to sample", kv("sampleUnitId", sampleUnitId));
          } catch (Exception exc) {
            LOG.error("unexpected exception when calling party service", exc);
          }
        });
  }

  private void addPartyIdToSample(String sampleUnitId, PartyDTO party) {
    try {
      LOG.debug(
          "add party to sample", kv("sampleUnitId", sampleUnitId), kv("partyId", party.getId()));
      UUID partyId = UUID.fromString(party.getId());
      SampleUnit sampleUnit =
          sampleUnitRepository.findById(UUID.fromString(sampleUnitId)).orElseThrow();
      sampleUnit.setPartyId(partyId);
      sampleUnitRepository.saveAndFlush(sampleUnit);
      LOG.debug(
          "party added", kv("sampleUnitId", sampleUnit.getId()), kv("partyId", party.getId()));
    } catch (RuntimeException e) {
      LOG.error(
          "Unexpected exception saving party id",
          kv("sampleUnitId", sampleUnitId),
          kv("partyId", party.getId()),
          e);
    }
  }
}
