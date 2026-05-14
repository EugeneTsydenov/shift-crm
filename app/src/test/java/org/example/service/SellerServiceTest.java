package org.example.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.example.exception.SellerNotFoundException;
import org.example.model.dto.seller.SellerRequest;
import org.example.model.dto.seller.SellerResponse;
import org.example.model.dto.seller.SellerUpdateRequest;
import org.example.model.entity.Seller;
import org.example.repository.SellerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SellerServiceTest {

    @Mock
    private SellerRepository sellerRepository;

    @InjectMocks
    private SellerService sellerService;

    @Test
    void findById_whenSellerExists_returnsResponse() {
        Seller seller = Seller.create("Ivan", "ivan@mail.ru");
        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));

        SellerResponse response = sellerService.findById(1L);

        assertThat(response.name()).isEqualTo("Ivan");
        assertThat(response.contactInfo()).isEqualTo("ivan@mail.ru");
    }

    @Test
    void findById_whenSellerNotExists_throwsException() {
        when(sellerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerService.findById(1L))
                .isInstanceOf(SellerNotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    void findById_callsRepositoryWithCorrectId() {
        Seller seller = Seller.create("Ivan", "ivan@mail.ru");
        when(sellerRepository.findById(42L)).thenReturn(Optional.of(seller));

        sellerService.findById(42L);

        verify(sellerRepository).findById(42L);
    }

    @Test
    void findAll_returnsMappedList() {
        Seller seller1 = Seller.create("Ivan", "ivan@mail.ru");
        Seller seller2 = Seller.create("Petr", "petr@mail.ru");
        when(sellerRepository.findAll()).thenReturn(List.of(seller1, seller2));

        List<SellerResponse> responses = sellerService.findAll();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).name()).isEqualTo("Ivan");
        assertThat(responses.get(1).name()).isEqualTo("Petr");
    }

    @Test
    void findAll_whenEmpty_returnsEmptyList() {
        when(sellerRepository.findAll()).thenReturn(List.of());

        List<SellerResponse> responses = sellerService.findAll();

        assertThat(responses).isEmpty();
    }

    @Test
    void findAll_mapsAllFields() {
        Seller seller = Seller.create("Ivan", "ivan@mail.ru");
        when(sellerRepository.findAll()).thenReturn(List.of(seller));

        List<SellerResponse> responses = sellerService.findAll();

        assertThat(responses.get(0).name()).isEqualTo("Ivan");
        assertThat(responses.get(0).contactInfo()).isEqualTo("ivan@mail.ru");
        assertThat(responses.get(0).registrationDate()).isNotNull();
    }

    @Test
    void create_savesAndReturnsResponse() {
        SellerRequest request = new SellerRequest("Ivan", "ivan@mail.ru");
        Seller seller = Seller.create("Ivan", "ivan@mail.ru");
        when(sellerRepository.save(any())).thenReturn(seller);

        SellerResponse response = sellerService.create(request);

        assertThat(response.name()).isEqualTo("Ivan");
        assertThat(response.contactInfo()).isEqualTo("ivan@mail.ru");
    }

    @Test
    void create_setsRegistrationDate() {
        SellerRequest request = new SellerRequest("Ivan", "ivan@mail.ru");
        Seller seller = Seller.create("Ivan", "ivan@mail.ru");
        when(sellerRepository.save(any())).thenReturn(seller);

        SellerResponse response = sellerService.create(request);

        assertThat(response.registrationDate()).isNotNull();
    }

    @Test
    void create_callsSaveExactlyOnce() {
        SellerRequest request = new SellerRequest("Ivan", "ivan@mail.ru");
        Seller seller = Seller.create("Ivan", "ivan@mail.ru");
        when(sellerRepository.save(any())).thenReturn(seller);

        sellerService.create(request);

        verify(sellerRepository, times(1)).save(any());
    }

    @Test
    void update_whenSellerExists_updatesName() {
        Seller seller = Seller.create("Ivan", "ivan@mail.ru");
        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));

        SellerResponse response = sellerService.update(1L, new SellerUpdateRequest("Petr", null));

        assertThat(response.name()).isEqualTo("Petr");
        assertThat(response.contactInfo()).isEqualTo("ivan@mail.ru");
    }

    @Test
    void update_whenSellerExists_updatesContactInfo() {
        Seller seller = Seller.create("Ivan", "ivan@mail.ru");
        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));

        SellerResponse response = sellerService.update(1L, new SellerUpdateRequest(null, "petr@mail.ru"));

        assertThat(response.name()).isEqualTo("Ivan");
        assertThat(response.contactInfo()).isEqualTo("petr@mail.ru");
    }

    @Test
    void update_whenBothFieldsFilled_updatesBoth() {
        Seller seller = Seller.create("Ivan", "ivan@mail.ru");
        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));

        SellerResponse response = sellerService.update(1L, new SellerUpdateRequest("Petr", "petr@mail.ru"));

        assertThat(response.name()).isEqualTo("Petr");
        assertThat(response.contactInfo()).isEqualTo("petr@mail.ru");
    }

    @Test
    void update_whenBothFieldsNull_nothingChanges() {
        Seller seller = Seller.create("Ivan", "ivan@mail.ru");
        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));

        SellerResponse response = sellerService.update(1L, new SellerUpdateRequest(null, null));

        assertThat(response.name()).isEqualTo("Ivan");
        assertThat(response.contactInfo()).isEqualTo("ivan@mail.ru");
    }

    @Test
    void update_whenSellerNotExists_throwsException() {
        when(sellerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerService.update(1L, new SellerUpdateRequest("Petr", null)))
                .isInstanceOf(SellerNotFoundException.class);
    }

    @Test
    void update_doesNotCallSaveExplicitly() {
        Seller seller = Seller.create("Ivan", "ivan@mail.ru");
        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));

        sellerService.update(1L, new SellerUpdateRequest("Petr", null));

        verify(sellerRepository, never()).save(any());
    }

    @Test
    void delete_whenSellerExists_setsDeletedAt() {
        Seller seller = Seller.create("Ivan", "ivan@mail.ru");
        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));

        sellerService.delete(1L);

        assertThat(seller.getDeletedAt()).isNotNull();
    }

    @Test
    void delete_setsDeletedAtToNow() {
        Seller seller = Seller.create("Ivan", "ivan@mail.ru");
        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));
        LocalDateTime before = LocalDateTime.now();

        sellerService.delete(1L);

        assertThat(seller.getDeletedAt()).isAfterOrEqualTo(before);
    }

    @Test
    void delete_callsSave() {
        Seller seller = Seller.create("Ivan", "ivan@mail.ru");
        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));

        sellerService.delete(1L);

        verify(sellerRepository).save(seller);
    }

    @Test
    void delete_whenSellerNotExists_throwsException() {
        when(sellerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerService.delete(1L))
                .isInstanceOf(SellerNotFoundException.class);
    }
}