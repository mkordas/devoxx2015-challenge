package pl.allegro.promo.devoxx2015.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import pl.allegro.promo.devoxx2015.domain.Offer;
import pl.allegro.promo.devoxx2015.domain.OfferRepository;
import pl.allegro.promo.devoxx2015.domain.PhotoScoreSource;

import java.util.List;

@Component
public class OfferService {

    private final OfferRepository offerRepository;
    private final PhotoScoreSource photoScoreSource;

    @Autowired
    public OfferService(OfferRepository offerRepository, PhotoScoreSource photoScoreSource) {
        this.offerRepository = offerRepository;
        this.photoScoreSource = photoScoreSource;
    }

    public void processOffers(List<OfferPublishedEvent> events) {
        events.stream().map(e -> new Offer(e.getId(), e.getTitle(), e.getPhotoUrl(), getScore(e)))
                .filter(Offer::hasPrettyPhoto)
                .sorted((o1, o2) -> Double.compare(o2.getPhotoScore(), o1.getPhotoScore()))
                .forEach(offerRepository::save);
    }

    private double getScore(OfferPublishedEvent offerPublishedEvent) {
        try {
            return photoScoreSource.getScore(offerPublishedEvent.getPhotoUrl());
        } catch (RestClientException ignored) {
            return 0.7;
        }
    }

    public List<Offer> getOffers() {
        return offerRepository.findAll(); // TODO some sorting?
    }
}
