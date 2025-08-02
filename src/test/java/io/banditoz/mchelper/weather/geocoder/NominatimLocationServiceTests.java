//package io.banditoz.mchelper.weather.geocoder;
//
//import io.banditoz.mchelper.commands.BaseCommandTest;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.Test;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.within;
//
///**
// * Class to test the {@link NominatimLocationService}. This does not mock responses, and makes real calls.
// */
//public class NominatimLocationServiceTests extends BaseCommandTest { // BaseCommandTest gives us an MCHelper instance to use.
//    private NominatimLocationService locationService;
//
//    @BeforeClass
//    public void setup() {
//        this.locationService = mcHelper.getNominatimLocationService();
//    }
//
//    @Test
//    public void testSearchForLocation_happyPath() {
//        List<Location> locs = locationService.searchForLocation("KSLC");
//        assertThat(locs).hasSize(1);
//        Location l = locs.get(0);
//
//        // sanity checks
//        assertThat(l.displayName()).contains("Utah");
//        assertThat(Double.parseDouble(l.lat())).isCloseTo(40.790066100000004, within(0.01));
//        assertThat(Double.parseDouble(l.lon())).isCloseTo(-111.97989846185591, within(0.01));
//    }
//
//    @Test
//    public void testSearchForLocation_noLocationFound() {
//        List<Location> locs = locationService.searchForLocation("ThisBetterNotBeAPlaceOnThisPlanet!");
//        assertThat(locs).isEmpty();
//    }
//}