package id.ac.ui.cs.advprog.everest.modules.repairorder.service;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.common.service.UserServiceGrpcClient;
import id.ac.ui.cs.advprog.everest.modules.coupon.model.Coupon;
import id.ac.ui.cs.advprog.everest.modules.coupon.repository.CouponRepository;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.model.PaymentMethod;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.model.enums.PaymentType;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.repository.PaymentMethodRepository;
import id.ac.ui.cs.advprog.everest.modules.repairorder.dto.CreateAndUpdateRepairOrderRequest;
import id.ac.ui.cs.advprog.everest.modules.repairorder.dto.ViewRepairOrderResponse;
import id.ac.ui.cs.advprog.everest.modules.repairorder.exception.DatabaseException;
import id.ac.ui.cs.advprog.everest.modules.repairorder.exception.InvalidRepairOrderStateException;
import id.ac.ui.cs.advprog.everest.modules.repairorder.exception.TechnicianUnavailableException;
import id.ac.ui.cs.advprog.everest.modules.repairorder.model.RepairOrder;
import id.ac.ui.cs.advprog.everest.modules.repairorder.model.enums.RepairOrderStatus;
import id.ac.ui.cs.advprog.everest.modules.repairorder.repository.RepairOrderRepository;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.GetRandomTechnicianResponse;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserIdentity;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserData;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepairOrderServiceImplTest {
    @Mock
    private UserServiceGrpcClient userServiceGrpcClient;
    @Mock
    private RepairOrderRepository repairOrderRepository;
    @Mock
    private CouponRepository couponRepository;
    @Mock
    private PaymentMethodRepository paymentMethodRepository;
    @InjectMocks
    private RepairOrderServiceImpl repairOrderService;

    private UUID customerId;
    private UUID technicianId;
    private UUID paymentMethodId;
    private UUID couponId;
    private AuthenticatedUser customer;
    private CreateAndUpdateRepairOrderRequest validRequest;
    private RepairOrder sampleRepairOrder;
    private GetRandomTechnicianResponse randomTechnicianResponse;
    private PaymentMethod paymentMethod;
    private Coupon coupon;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        technicianId = UUID.randomUUID();
        paymentMethodId = UUID.randomUUID();
        couponId = UUID.randomUUID();

        customer = new AuthenticatedUser(
                customerId,
                "customer@example.com",
                "Customer",
                UserRole.CUSTOMER,
                "12301894239",
                Instant.now(),
                Instant.now(),
                "Depok",
                null,
                0,
                0L
        );

        LocalDate now = LocalDate.now();
        validRequest = new CreateAndUpdateRepairOrderRequest();
        validRequest.setItemName("Laptop");
        validRequest.setItemCondition("Screen not working");
        validRequest.setIssueDescription("Black screen after startup");
        validRequest.setDesiredServiceDate(now.plusDays(2));
        validRequest.setPaymentMethodId(paymentMethodId);
        validRequest.setCouponId(couponId);

        // Set up payment method
        paymentMethod = PaymentMethod.builder()
                .id(paymentMethodId)
                .name("Credit Card")
                .type(PaymentType.BANK_TRANSFER)
                .provider("Bank XYZ")
                .accountNumber("123456789")
                .accountName("John Doe")
                .build();

        // Set up coupon
        coupon = Coupon.builder()
                .id(couponId)
                .code("DISCOUNT10")
                .discountAmount(10)
                .maxUsage(5)
                .validUntil(LocalDate.now())
                .build();

        // Set up technician response
        UserIdentity technicianIdentity = UserIdentity.newBuilder()
                .setId(technicianId.toString())
                .build();
        UserData technicianData = UserData.newBuilder()
                .setIdentity(technicianIdentity)
                .build();
        randomTechnicianResponse = GetRandomTechnicianResponse.newBuilder()
                .setTechnician(technicianData)
                .build();

        // Sample repair order
        sampleRepairOrder = RepairOrder.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .technicianId(technicianId)
                .status(RepairOrderStatus.PENDING_CONFIRMATION)
                .itemName(validRequest.getItemName())
                .itemCondition(validRequest.getItemCondition())
                .issueDescription(validRequest.getIssueDescription())
                .desiredServiceDate(validRequest.getDesiredServiceDate())
                .paymentMethod(paymentMethod)
                .coupon(coupon)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // CREATE REPAIR ORDER TESTS
    @Test
    void createRepairOrder_Success() {
        // Arrange
        when(userServiceGrpcClient.getRandomTechnician()).thenReturn(randomTechnicianResponse);
        when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Optional.of(paymentMethod));
        when(couponRepository.findById(couponId)).thenReturn(Optional.of(coupon));
        when(repairOrderRepository.save(any(RepairOrder.class))).thenReturn(sampleRepairOrder);

        // Act
        GenericResponse<ViewRepairOrderResponse> response = repairOrderService.createRepairOrder(validRequest, customer);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Repair order created successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(sampleRepairOrder.getId(), response.getData().getId());
        assertEquals(customerId, response.getData().getCustomerId());
        assertEquals(technicianId, response.getData().getTechnicianId());
        assertEquals(RepairOrderStatus.PENDING_CONFIRMATION, response.getData().getStatus());
        assertEquals(validRequest.getItemName(), response.getData().getItemName());
        assertEquals(paymentMethodId, response.getData().getPaymentMethodId());
        assertEquals(couponId, response.getData().getCouponId());

        verify(userServiceGrpcClient).getRandomTechnician();
        verify(paymentMethodRepository).findById(paymentMethodId);
        verify(couponRepository).findById(couponId);
        verify(repairOrderRepository).save(any(RepairOrder.class));
    }

    @Test
    void createRepairOrder_WithoutCoupon_Success() {
        // Arrange
        validRequest.setCouponId(null);

        when(userServiceGrpcClient.getRandomTechnician()).thenReturn(randomTechnicianResponse);
        when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Optional.of(paymentMethod));

        RepairOrder orderWithoutCoupon = RepairOrder.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .technicianId(technicianId)
                .status(RepairOrderStatus.PENDING_CONFIRMATION)
                .itemName(validRequest.getItemName())
                .itemCondition(validRequest.getItemCondition())
                .issueDescription(validRequest.getIssueDescription())
                .desiredServiceDate(validRequest.getDesiredServiceDate())
                .paymentMethod(paymentMethod)
                .coupon(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(repairOrderRepository.save(any(RepairOrder.class))).thenReturn(orderWithoutCoupon);

        // Act
        GenericResponse<ViewRepairOrderResponse> response = repairOrderService.createRepairOrder(validRequest, customer);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Repair order created successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(orderWithoutCoupon.getId(), response.getData().getId());
        assertNull(response.getData().getCouponId());

        verify(userServiceGrpcClient).getRandomTechnician();
        verify(paymentMethodRepository).findById(paymentMethodId);
        verify(couponRepository, never()).findById(any(UUID.class));
        verify(repairOrderRepository).save(any(RepairOrder.class));
    }

    @Test
    void createRepairOrder_InvalidPaymentMethod_ThrowsException() {
        // Arrange
        when(userServiceGrpcClient.getRandomTechnician()).thenReturn(randomTechnicianResponse);
        when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.createRepairOrder(validRequest, customer)
        );

        verify(userServiceGrpcClient).getRandomTechnician();
        verify(paymentMethodRepository).findById(paymentMethodId);
        verify(couponRepository, never()).findById(any(UUID.class));
        verify(repairOrderRepository, never()).save(any(RepairOrder.class));
    }

    @Test
    void createRepairOrder_InvalidCoupon_ThrowsException() {
        // Arrange
        when(userServiceGrpcClient.getRandomTechnician()).thenReturn(randomTechnicianResponse);
        when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Optional.of(paymentMethod));
        when(couponRepository.findById(couponId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.createRepairOrder(validRequest, customer)
        );

        verify(userServiceGrpcClient).getRandomTechnician();
        verify(paymentMethodRepository).findById(paymentMethodId);
        verify(couponRepository).findById(couponId);
        verify(repairOrderRepository, never()).save(any(RepairOrder.class));
    }

    @Test
    void createRepairOrder_NullRequest_ThrowsException() {
        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.createRepairOrder(null, customer)
        );
        verifyNoInteractions(userServiceGrpcClient);
        verifyNoInteractions(paymentMethodRepository);
        verifyNoInteractions(couponRepository);
        verifyNoInteractions(repairOrderRepository);
    }

    @Test
    void createRepairOrder_NullCustomer_ThrowsException() {
        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.createRepairOrder(validRequest, null)
        );
        verifyNoInteractions(userServiceGrpcClient);
        verifyNoInteractions(paymentMethodRepository);
        verifyNoInteractions(couponRepository);
        verifyNoInteractions(repairOrderRepository);
    }

    @Test
    void createRepairOrder_NoTechnicianAvailable_ThrowsException() {
        // Arrange
        GetRandomTechnicianResponse emptyResponse = GetRandomTechnicianResponse.newBuilder().build();
        when(userServiceGrpcClient.getRandomTechnician()).thenReturn(emptyResponse);

        // Act & Assert
        assertThrows(TechnicianUnavailableException.class, () ->
                repairOrderService.createRepairOrder(validRequest, customer)
        );

        verify(userServiceGrpcClient).getRandomTechnician();
        verifyNoInteractions(paymentMethodRepository);
        verifyNoInteractions(couponRepository);
        verifyNoInteractions(repairOrderRepository);
    }

    @Test
    void createRepairOrder_DatabaseException_ThrowsException() {
        // Arrange
        when(userServiceGrpcClient.getRandomTechnician()).thenReturn(randomTechnicianResponse);
        when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Optional.of(paymentMethod));
        when(couponRepository.findById(couponId)).thenReturn(Optional.of(coupon));
        when(repairOrderRepository.save(any(RepairOrder.class))).thenThrow(mock(DataAccessException.class));

        // Act & Assert
        assertThrows(DatabaseException.class, () ->
                repairOrderService.createRepairOrder(validRequest, customer)
        );

        verify(userServiceGrpcClient).getRandomTechnician();
        verify(paymentMethodRepository).findById(paymentMethodId);
        verify(couponRepository).findById(couponId);
        verify(repairOrderRepository).save(any(RepairOrder.class));
    }

    // GET REPAIR ORDERS TESTS
    @Test
    void getRepairOrders_Success() {
        // Arrange
        List<RepairOrder> repairOrders = Collections.singletonList(sampleRepairOrder);
        when(repairOrderRepository.findByCustomerId(customerId)).thenReturn(repairOrders);

        // Act
        GenericResponse<List<ViewRepairOrderResponse>> response = repairOrderService.getRepairOrders(customer);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Repair orders retrieved successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
        assertEquals(sampleRepairOrder.getId(), response.getData().getFirst().getId());
        assertEquals(paymentMethodId, response.getData().getFirst().getPaymentMethodId());
        assertEquals(couponId, response.getData().getFirst().getCouponId());

        verify(repairOrderRepository).findByCustomerId(customerId);
    }

    @Test
    void getRepairOrders_EmptyList_Success() {
        // Arrange
        List<RepairOrder> emptyList = List.of();
        when(repairOrderRepository.findByCustomerId(customerId)).thenReturn(emptyList);

        // Act
        GenericResponse<List<ViewRepairOrderResponse>> response = repairOrderService.getRepairOrders(customer);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Repair orders retrieved successfully", response.getMessage());
        assertNotNull(response.getData());
        assertTrue(response.getData().isEmpty());

        verify(repairOrderRepository).findByCustomerId(customerId);
    }

    @Test
    void getRepairOrders_NullCustomer_ThrowsException() {
        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.getRepairOrders(null)
        );
        verifyNoInteractions(repairOrderRepository);
    }

    @Test
    void getRepairOrders_DatabaseException_ThrowsException() {
        // Arrange
        when(repairOrderRepository.findByCustomerId(customerId)).thenThrow(mock(DataAccessException.class));

        // Act & Assert
        assertThrows(DatabaseException.class, () ->
                repairOrderService.getRepairOrders(customer)
        );

        verify(repairOrderRepository).findByCustomerId(customerId);
    }

    // UPDATE REPAIR ORDER TESTS
    @Test
    void updateRepairOrder_Success() {
        // Arrange
        String repairOrderId = sampleRepairOrder.getId().toString();

        // Setup new payment method and coupon for update
        UUID newPaymentMethodId = UUID.randomUUID();
        UUID newCouponId = UUID.randomUUID();

        PaymentMethod newPaymentMethod = PaymentMethod.builder()
                .id(newPaymentMethodId)
                .name("Debit Card")
                .type(PaymentType.BANK_TRANSFER)
                .provider("Bank XYZ")
                .accountNumber("123456789")
                .accountName("John Doe")
                .build();

        Coupon newCoupon = Coupon.builder()
                .id(newCouponId)
                .code("DISCOUNT20")
                .discountAmount(20)
                .maxUsage(5)
                .validUntil(LocalDate.now())
                .build();

        // Update request with new values
        CreateAndUpdateRepairOrderRequest updateRequest = new CreateAndUpdateRepairOrderRequest();
        updateRequest.setItemName("Updated Laptop");
        updateRequest.setItemCondition("Updated Screen not working");
        updateRequest.setIssueDescription("Updated Black screen after startup");
        updateRequest.setDesiredServiceDate(LocalDate.now().plusDays(5));
        updateRequest.setPaymentMethodId(newPaymentMethodId);
        updateRequest.setCouponId(newCouponId);

        when(repairOrderRepository.findById(sampleRepairOrder.getId())).thenReturn(Optional.of(sampleRepairOrder));
        when(paymentMethodRepository.findById(newPaymentMethodId)).thenReturn(Optional.of(newPaymentMethod));
        when(couponRepository.findById(newCouponId)).thenReturn(Optional.of(newCoupon));

        RepairOrder updatedOrder = RepairOrder.builder()
                .id(sampleRepairOrder.getId())
                .customerId(customerId)
                .technicianId(technicianId)
                .status(RepairOrderStatus.PENDING_CONFIRMATION)
                .itemName(updateRequest.getItemName())
                .itemCondition(updateRequest.getItemCondition())
                .issueDescription(updateRequest.getIssueDescription())
                .desiredServiceDate(updateRequest.getDesiredServiceDate())
                .paymentMethod(newPaymentMethod)
                .coupon(newCoupon)
                .createdAt(sampleRepairOrder.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(repairOrderRepository.save(any(RepairOrder.class))).thenReturn(updatedOrder);

        // Act
        GenericResponse<ViewRepairOrderResponse> response = repairOrderService.updateRepairOrder(
                repairOrderId, updateRequest, customer);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Repair order updated successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(sampleRepairOrder.getId(), response.getData().getId());
        assertEquals(updateRequest.getItemName(), response.getData().getItemName());
        assertEquals(newPaymentMethodId, response.getData().getPaymentMethodId());
        assertEquals(newCouponId, response.getData().getCouponId());

        verify(repairOrderRepository).findById(sampleRepairOrder.getId());
        verify(paymentMethodRepository).findById(newPaymentMethodId);
        verify(couponRepository).findById(newCouponId);
        verify(repairOrderRepository).save(any(RepairOrder.class));
    }

    @Test
    void updateRepairOrder_RemoveCoupon_Success() {
        // Arrange
        String repairOrderId = sampleRepairOrder.getId().toString();

        // Update request with no coupon
        CreateAndUpdateRepairOrderRequest updateRequest = new CreateAndUpdateRepairOrderRequest();
        updateRequest.setItemName("Updated Laptop");
        updateRequest.setItemCondition("Updated Screen not working");
        updateRequest.setIssueDescription("Updated Black screen after startup");
        updateRequest.setDesiredServiceDate(LocalDate.now().plusDays(5));
        updateRequest.setPaymentMethodId(paymentMethodId);
        updateRequest.setCouponId(null); // No coupon

        when(repairOrderRepository.findById(sampleRepairOrder.getId())).thenReturn(Optional.of(sampleRepairOrder));
        when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Optional.of(paymentMethod));

        RepairOrder updatedOrder = RepairOrder.builder()
                .id(sampleRepairOrder.getId())
                .customerId(customerId)
                .technicianId(technicianId)
                .status(RepairOrderStatus.PENDING_CONFIRMATION)
                .itemName(updateRequest.getItemName())
                .itemCondition(updateRequest.getItemCondition())
                .issueDescription(updateRequest.getIssueDescription())
                .desiredServiceDate(updateRequest.getDesiredServiceDate())
                .paymentMethod(paymentMethod)
                .coupon(null) // No coupon
                .createdAt(sampleRepairOrder.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(repairOrderRepository.save(any(RepairOrder.class))).thenReturn(updatedOrder);

        // Act
        GenericResponse<ViewRepairOrderResponse> response = repairOrderService.updateRepairOrder(
                repairOrderId, updateRequest, customer);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Repair order updated successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(sampleRepairOrder.getId(), response.getData().getId());
        assertEquals(updateRequest.getItemName(), response.getData().getItemName());
        assertEquals(paymentMethodId, response.getData().getPaymentMethodId());
        assertNull(response.getData().getCouponId());

        verify(repairOrderRepository).findById(sampleRepairOrder.getId());
        verify(paymentMethodRepository).findById(paymentMethodId);
        verify(couponRepository, never()).findById(any(UUID.class));
        verify(repairOrderRepository).save(any(RepairOrder.class));
    }

    @Test
    void updateRepairOrder_InvalidPaymentMethod_ThrowsException() {
        // Arrange
        String repairOrderId = sampleRepairOrder.getId().toString();

        when(repairOrderRepository.findById(sampleRepairOrder.getId())).thenReturn(Optional.of(sampleRepairOrder));
        when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.updateRepairOrder(repairOrderId, validRequest, customer)
        );

        verify(repairOrderRepository).findById(sampleRepairOrder.getId());
        verify(paymentMethodRepository).findById(paymentMethodId);
        verify(repairOrderRepository, never()).save(any(RepairOrder.class));
    }

    @Test
    void updateRepairOrder_InvalidCoupon_ThrowsException() {
        // Arrange
        String repairOrderId = sampleRepairOrder.getId().toString();

        when(repairOrderRepository.findById(sampleRepairOrder.getId())).thenReturn(Optional.of(sampleRepairOrder));
        when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Optional.of(paymentMethod));
        when(couponRepository.findById(couponId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.updateRepairOrder(repairOrderId, validRequest, customer)
        );

        verify(repairOrderRepository).findById(sampleRepairOrder.getId());
        verify(paymentMethodRepository).findById(paymentMethodId);
        verify(couponRepository).findById(couponId);
        verify(repairOrderRepository, never()).save(any(RepairOrder.class));
    }

    @Test
    void updateRepairOrder_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.updateRepairOrder(null, validRequest, customer)
        );
        verifyNoInteractions(repairOrderRepository);
        verifyNoInteractions(paymentMethodRepository);
        verifyNoInteractions(couponRepository);
    }

    @Test
    void updateRepairOrder_NullRequest_ThrowsException() {
        // Arrange
        String repairOrderId = sampleRepairOrder.getId().toString();

        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.updateRepairOrder(repairOrderId, null, customer)
        );
        verifyNoInteractions(repairOrderRepository);
        verifyNoInteractions(paymentMethodRepository);
        verifyNoInteractions(couponRepository);
    }

    @Test
    void updateRepairOrder_NullCustomer_ThrowsException() {
        // Arrange
        String repairOrderId = sampleRepairOrder.getId().toString();

        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.updateRepairOrder(repairOrderId, validRequest, null)
        );
        verifyNoInteractions(repairOrderRepository);
        verifyNoInteractions(paymentMethodRepository);
        verifyNoInteractions(couponRepository);
    }

    @Test
    void updateRepairOrder_InvalidUUID_ThrowsException() {
        // Arrange
        String invalidId = "not-a-uuid";

        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.updateRepairOrder(invalidId, validRequest, customer)
        );
        verifyNoInteractions(repairOrderRepository);
        verifyNoInteractions(paymentMethodRepository);
        verifyNoInteractions(couponRepository);
    }

    @Test
    void updateRepairOrder_OrderNotFound_ThrowsException() {
        // Arrange
        String repairOrderId = UUID.randomUUID().toString();
        when(repairOrderRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.updateRepairOrder(repairOrderId, validRequest, customer)
        );

        verify(repairOrderRepository).findById(any(UUID.class));
        verifyNoInteractions(paymentMethodRepository);
        verifyNoInteractions(couponRepository);
        verify(repairOrderRepository, never()).save(any(RepairOrder.class));
    }

    @Test
    void updateRepairOrder_UnauthorizedCustomer_ThrowsException() {
        // Arrange
        String repairOrderId = sampleRepairOrder.getId().toString();
        UUID differentCustomerId = UUID.randomUUID();
        AuthenticatedUser differentCustomer = new AuthenticatedUser(
                differentCustomerId,
                "customer@example.com",
                "Customer",
                UserRole.CUSTOMER,
                "12301894239",
                Instant.now(),
                Instant.now(),
                "Depok",
                null,
                0,
                0L
        );
        when(repairOrderRepository.findById(sampleRepairOrder.getId())).thenReturn(Optional.of(sampleRepairOrder));

        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.updateRepairOrder(repairOrderId, validRequest, differentCustomer)
        );

        verify(repairOrderRepository).findById(sampleRepairOrder.getId());
        verifyNoInteractions(paymentMethodRepository);
        verifyNoInteractions(couponRepository);
        verify(repairOrderRepository, never()).save(any(RepairOrder.class));
    }

    @Test
    void updateRepairOrder_InvalidStatus_ThrowsException() {
        // Arrange
        String repairOrderId = sampleRepairOrder.getId().toString();
        RepairOrder inProgressOrder = sampleRepairOrder;
        inProgressOrder.setStatus(RepairOrderStatus.IN_PROGRESS);
        when(repairOrderRepository.findById(sampleRepairOrder.getId())).thenReturn(Optional.of(inProgressOrder));

        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.updateRepairOrder(repairOrderId, validRequest, customer)
        );

        verify(repairOrderRepository).findById(sampleRepairOrder.getId());
        verifyNoInteractions(paymentMethodRepository);
        verifyNoInteractions(couponRepository);
        verify(repairOrderRepository, never()).save(any(RepairOrder.class));
    }

    @Test
    void updateRepairOrder_DatabaseException_ThrowsException() {
        // Arrange
        String repairOrderId = sampleRepairOrder.getId().toString();
        when(repairOrderRepository.findById(sampleRepairOrder.getId())).thenReturn(Optional.of(sampleRepairOrder));
        when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Optional.of(paymentMethod));
        when(couponRepository.findById(couponId)).thenReturn(Optional.of(coupon));
        when(repairOrderRepository.save(any(RepairOrder.class))).thenThrow(mock(DataAccessException.class));

        // Act & Assert
        assertThrows(DatabaseException.class, () ->
                repairOrderService.updateRepairOrder(repairOrderId, validRequest, customer)
        );

        verify(repairOrderRepository).findById(sampleRepairOrder.getId());
        verify(paymentMethodRepository).findById(paymentMethodId);
        verify(couponRepository).findById(couponId);
        verify(repairOrderRepository).save(any(RepairOrder.class));
    }

    // DELETE REPAIR ORDER TESTS
    @Test
    void deleteRepairOrder_Success() {
        // Arrange
        String repairOrderId = sampleRepairOrder.getId().toString();
        when(repairOrderRepository.findById(sampleRepairOrder.getId())).thenReturn(Optional.of(sampleRepairOrder));
        doNothing().when(repairOrderRepository).delete(sampleRepairOrder);
        // Act
        GenericResponse<Void> response = repairOrderService.deleteRepairOrder(repairOrderId, customer);
        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Repair order deleted successfully", response.getMessage());
        assertNull(response.getData());
        verify(repairOrderRepository).findById(sampleRepairOrder.getId());
        verify(repairOrderRepository).delete(sampleRepairOrder);
    }
    @Test
    void deleteRepairOrder_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.deleteRepairOrder(null, customer)
        );
        verifyNoInteractions(repairOrderRepository);
    }
    @Test
    void deleteRepairOrder_NullCustomer_ThrowsException() {
        // Arrange
        String repairOrderId = sampleRepairOrder.getId().toString();
        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.deleteRepairOrder(repairOrderId, null)
        );
        verifyNoInteractions(repairOrderRepository);
    }
    @Test
    void deleteRepairOrder_InvalidUUID_ThrowsException() {
        // Arrange
        String invalidId = "not-a-uuid";
        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.deleteRepairOrder(invalidId, customer)
        );
        verifyNoInteractions(repairOrderRepository);
    }
    @Test
    void deleteRepairOrder_OrderNotFound_ThrowsException() {
        // Arrange
        String repairOrderId = UUID.randomUUID().toString();
        when(repairOrderRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.deleteRepairOrder(repairOrderId, customer)
        );
        verify(repairOrderRepository).findById(any(UUID.class));
        verify(repairOrderRepository, never()).delete(any(RepairOrder.class));
    }
    @Test
    void deleteRepairOrder_UnauthorizedCustomer_ThrowsException() {
        // Arrange
        String repairOrderId = sampleRepairOrder.getId().toString();
        UUID differentCustomerId = UUID.randomUUID();
        AuthenticatedUser differentCustomer = new AuthenticatedUser(
                differentCustomerId,
                "customer@example.com",
                "Customer",
                UserRole.CUSTOMER,
                "12301894239",
                Instant.now(),
                Instant.now(),
                "Depok",
                null,
                0,
                0L
        );
        when(repairOrderRepository.findById(sampleRepairOrder.getId())).thenReturn(Optional.of(sampleRepairOrder));
        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.deleteRepairOrder(repairOrderId, differentCustomer)
        );
        verify(repairOrderRepository).findById(sampleRepairOrder.getId());
        verify(repairOrderRepository, never()).delete(any(RepairOrder.class));
    }
    @Test
    void deleteRepairOrder_InvalidStatus_ThrowsException() {
        // Arrange
        String repairOrderId = sampleRepairOrder.getId().toString();
        RepairOrder inProgressOrder = sampleRepairOrder;
        inProgressOrder.setStatus(RepairOrderStatus.IN_PROGRESS);
        when(repairOrderRepository.findById(sampleRepairOrder.getId())).thenReturn(Optional.of(inProgressOrder));
        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.deleteRepairOrder(repairOrderId, customer)
        );
        verify(repairOrderRepository).findById(sampleRepairOrder.getId());
        verify(repairOrderRepository, never()).delete(any(RepairOrder.class));
    }
    @Test
    void deleteRepairOrder_DatabaseException_ThrowsException() {
        // Arrange
        String repairOrderId = sampleRepairOrder.getId().toString();
        when(repairOrderRepository.findById(sampleRepairOrder.getId())).thenReturn(Optional.of(sampleRepairOrder));
        doThrow(mock(DataAccessException.class)).when(repairOrderRepository).delete(any(RepairOrder.class));
        // Act & Assert
        assertThrows(DatabaseException.class, () ->
                repairOrderService.deleteRepairOrder(repairOrderId, customer)
        );
        verify(repairOrderRepository).findById(sampleRepairOrder.getId());
        verify(repairOrderRepository).delete(any(RepairOrder.class));
    }
}