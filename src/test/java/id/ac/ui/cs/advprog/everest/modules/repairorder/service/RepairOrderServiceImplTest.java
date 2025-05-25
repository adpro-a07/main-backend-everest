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
import org.springframework.dao.OptimisticLockingFailureException;
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
        validRequest.setCouponCode("DISCOUNT10");

        // Set up payment method
        paymentMethod = PaymentMethod.builder()
                .id(paymentMethodId)
                .type(PaymentType.BANK_TRANSFER)
                .provider("Bank XYZ")
                .accountNumber("123456789")
                .accountName("John Doe")
                .build();

        // Set up coupon with usage tracking
        coupon = Coupon.builder()
                .id(couponId)
                .code("DISCOUNT10")
                .discountAmount(10)
                .maxUsage(5)
                .usageCount(2) // Current usage count
                .validUntil(LocalDate.now().plusDays(30)) // Valid coupon
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
        when(couponRepository.findByCode("DISCOUNT10")).thenReturn(Optional.of(coupon));
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
        verify(couponRepository).findByCode("DISCOUNT10");
        verify(couponRepository).saveAndFlush(coupon); // Verify usage count increment
        verify(repairOrderRepository).save(any(RepairOrder.class));
        assertEquals(3, coupon.getUsageCount()); // Verify usage count was incremented
    }

    @Test
    void createRepairOrder_WithoutCoupon_Success() {
        // Arrange
        validRequest.setCouponCode(null);
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
        verify(couponRepository, never()).findByCode(any(String.class));
        verify(couponRepository, never()).saveAndFlush(any(Coupon.class));
        verify(repairOrderRepository).save(any(RepairOrder.class));
    }

    @Test
    void createRepairOrder_CouponNotFound_ThrowsException() {
        // Arrange
        when(userServiceGrpcClient.getRandomTechnician()).thenReturn(randomTechnicianResponse);
        when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Optional.of(paymentMethod));
        when(couponRepository.findByCode("DISCOUNT10")).thenReturn(Optional.empty());

        // Act & Assert
        InvalidRepairOrderStateException exception = assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.createRepairOrder(validRequest, customer)
        );
        assertEquals("Coupon not found", exception.getMessage());

        verify(userServiceGrpcClient).getRandomTechnician();
        verify(paymentMethodRepository).findById(paymentMethodId);
        verify(couponRepository).findByCode("DISCOUNT10");
        verify(couponRepository, never()).saveAndFlush(any(Coupon.class));
        verify(repairOrderRepository, never()).save(any(RepairOrder.class));
    }

    @Test
    void createRepairOrder_ExpiredCoupon_ThrowsException() {
        // Arrange
        Coupon expiredCoupon = Coupon.builder()
                .id(couponId)
                .code("DISCOUNT10")
                .discountAmount(10)
                .maxUsage(5)
                .usageCount(2)
                .validUntil(LocalDate.now().minusDays(1)) // Expired
                .build();

        when(userServiceGrpcClient.getRandomTechnician()).thenReturn(randomTechnicianResponse);
        when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Optional.of(paymentMethod));
        when(couponRepository.findByCode("DISCOUNT10")).thenReturn(Optional.of(expiredCoupon));

        // Act & Assert
        InvalidRepairOrderStateException exception = assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.createRepairOrder(validRequest, customer)
        );
        assertEquals("Coupon has expired", exception.getMessage());

        verify(userServiceGrpcClient).getRandomTechnician();
        verify(paymentMethodRepository).findById(paymentMethodId);
        verify(couponRepository).findByCode("DISCOUNT10");
        verify(couponRepository, never()).saveAndFlush(any(Coupon.class));
        verify(repairOrderRepository, never()).save(any(RepairOrder.class));
    }

    @Test
    void createRepairOrder_CouponMaxUsageReached_ThrowsException() {
        // Arrange
        Coupon maxUsedCoupon = Coupon.builder()
                .id(couponId)
                .code("DISCOUNT10")
                .discountAmount(10)
                .maxUsage(5)
                .usageCount(5) // At max usage
                .validUntil(LocalDate.now().plusDays(30))
                .build();

        when(userServiceGrpcClient.getRandomTechnician()).thenReturn(randomTechnicianResponse);
        when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Optional.of(paymentMethod));
        when(couponRepository.findByCode("DISCOUNT10")).thenReturn(Optional.of(maxUsedCoupon));

        // Act & Assert
        InvalidRepairOrderStateException exception = assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.createRepairOrder(validRequest, customer)
        );
        assertEquals("Coupon has reached its maximum usage limit", exception.getMessage());

        verify(userServiceGrpcClient).getRandomTechnician();
        verify(paymentMethodRepository).findById(paymentMethodId);
        verify(couponRepository).findByCode("DISCOUNT10");
        verify(couponRepository, never()).saveAndFlush(any(Coupon.class));
        verify(repairOrderRepository, never()).save(any(RepairOrder.class));
    }

    @Test
    void createRepairOrder_CouponOptimisticLockingFailure_ThrowsException() {
        // Arrange
        when(userServiceGrpcClient.getRandomTechnician()).thenReturn(randomTechnicianResponse);
        when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Optional.of(paymentMethod));
        when(couponRepository.findByCode("DISCOUNT10")).thenReturn(Optional.of(coupon));
        when(couponRepository.saveAndFlush(coupon)).thenThrow(new OptimisticLockingFailureException("Concurrent modification"));

        // Act & Assert
        InvalidRepairOrderStateException exception = assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.createRepairOrder(validRequest, customer)
        );
        assertEquals("Coupon usage conflict, please try again", exception.getMessage());

        verify(userServiceGrpcClient).getRandomTechnician();
        verify(paymentMethodRepository).findById(paymentMethodId);
        verify(couponRepository).findByCode("DISCOUNT10");
        verify(couponRepository).saveAndFlush(coupon);
        verify(repairOrderRepository, never()).save(any(RepairOrder.class));
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
        verify(couponRepository, never()).findByCode(any(String.class));
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
        when(couponRepository.findByCode("DISCOUNT10")).thenReturn(Optional.of(coupon));
        when(repairOrderRepository.save(any(RepairOrder.class))).thenThrow(mock(DataAccessException.class));

        // Act & Assert
        assertThrows(DatabaseException.class, () ->
                repairOrderService.createRepairOrder(validRequest, customer)
        );

        verify(userServiceGrpcClient).getRandomTechnician();
        verify(paymentMethodRepository).findById(paymentMethodId);
        verify(couponRepository).findByCode("DISCOUNT10");
        verify(couponRepository).saveAndFlush(coupon);
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

    @Test
    void getRepairOrderById_Success() {
        String repairOrderId = sampleRepairOrder.getId().toString();
        when(repairOrderRepository.findById(sampleRepairOrder.getId())).thenReturn(Optional.of(sampleRepairOrder));

        GenericResponse<ViewRepairOrderResponse> response = repairOrderService.getRepairOrderById(repairOrderId, customer);

        assertTrue(response.isSuccess());
        assertEquals("Repair order retrieved successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(sampleRepairOrder.getId(), response.getData().getId());
        assertEquals(customerId, response.getData().getCustomerId());

        verify(repairOrderRepository).findById(sampleRepairOrder.getId());
    }

    @Test
    void getRepairOrderById_NullId_ThrowsException() {
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.getRepairOrderById(null, customer)
        );

        verifyNoInteractions(repairOrderRepository);
    }

    @Test
    void getRepairOrderById_NullCustomer_ThrowsException() {
        String repairOrderId = sampleRepairOrder.getId().toString();

        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.getRepairOrderById(repairOrderId, null)
        );

        verifyNoInteractions(repairOrderRepository);
    }

    @Test
    void getRepairOrderById_InvalidUUID_ThrowsException() {
        String invalidId = "not-a-uuid";

        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.getRepairOrderById(invalidId, customer)
        );

        verifyNoInteractions(repairOrderRepository);
    }

    @Test
    void getRepairOrderById_OrderNotFound_ThrowsException() {
        String repairOrderId = UUID.randomUUID().toString();
        when(repairOrderRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.getRepairOrderById(repairOrderId, customer)
        );

        verify(repairOrderRepository).findById(any(UUID.class));
    }

    @Test
    void getRepairOrderById_UnauthorizedCustomer_ThrowsException() {
        String repairOrderId = sampleRepairOrder.getId().toString();
        UUID differentCustomerId = UUID.randomUUID();
        AuthenticatedUser differentCustomer = new AuthenticatedUser(
                differentCustomerId,
                "other@example.com",
                "Other",
                UserRole.CUSTOMER,
                "1234567890",
                Instant.now(),
                Instant.now(),
                "Jakarta",
                null,
                0,
                0L
        );

        when(repairOrderRepository.findById(sampleRepairOrder.getId())).thenReturn(Optional.of(sampleRepairOrder));

        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.getRepairOrderById(repairOrderId, differentCustomer)
        );

        verify(repairOrderRepository).findById(sampleRepairOrder.getId());
    }

    @Test
    void getRepairOrderById_DatabaseException_ThrowsException() {
        String repairOrderId = sampleRepairOrder.getId().toString();
        when(repairOrderRepository.findById(sampleRepairOrder.getId())).thenThrow(mock(DataAccessException.class));

        assertThrows(DatabaseException.class, () ->
                repairOrderService.getRepairOrderById(repairOrderId, customer)
        );

        verify(repairOrderRepository).findById(sampleRepairOrder.getId());
    }

    // UPDATE REPAIR ORDER TESTS
    @Test
    void updateRepairOrder_Success_SameCoupon() {
        // Arrange
        String repairOrderId = sampleRepairOrder.getId().toString();

        when(repairOrderRepository.findById(sampleRepairOrder.getId())).thenReturn(Optional.of(sampleRepairOrder));
        when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Optional.of(paymentMethod));
        when(couponRepository.findByCode("DISCOUNT10")).thenReturn(Optional.of(coupon));

        RepairOrder updatedOrder = RepairOrder.builder()
                .id(sampleRepairOrder.getId())
                .customerId(customerId)
                .technicianId(technicianId)
                .status(RepairOrderStatus.PENDING_CONFIRMATION)
                .itemName(validRequest.getItemName())
                .itemCondition(validRequest.getItemCondition())
                .issueDescription(validRequest.getIssueDescription())
                .desiredServiceDate(validRequest.getDesiredServiceDate())
                .paymentMethod(paymentMethod)
                .coupon(coupon)
                .createdAt(sampleRepairOrder.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
        when(repairOrderRepository.save(any(RepairOrder.class))).thenReturn(updatedOrder);

        // Act
        GenericResponse<ViewRepairOrderResponse> response = repairOrderService.updateRepairOrder(
                repairOrderId, validRequest, customer);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Repair order updated successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(sampleRepairOrder.getId(), response.getData().getId());

        verify(repairOrderRepository).findById(sampleRepairOrder.getId());
        verify(paymentMethodRepository).findById(paymentMethodId);
        verify(couponRepository).findByCode("DISCOUNT10");
        // Should not increment usage count for same coupon
        verify(couponRepository, never()).saveAndFlush(any(Coupon.class));
        verify(repairOrderRepository).save(any(RepairOrder.class));
    }

    @Test
    void updateRepairOrder_Success_DifferentCoupon() {
        // Arrange
        String repairOrderId = sampleRepairOrder.getId().toString();

        // Setup new coupon
        UUID newCouponId = UUID.randomUUID();
        Coupon newCoupon = Coupon.builder()
                .id(newCouponId)
                .code("DISCOUNT20")
                .discountAmount(20)
                .maxUsage(5)
                .usageCount(1)
                .validUntil(LocalDate.now().plusDays(30))
                .build();

        CreateAndUpdateRepairOrderRequest updateRequest = new CreateAndUpdateRepairOrderRequest();
        updateRequest.setItemName("Updated Laptop");
        updateRequest.setItemCondition("Updated Screen not working");
        updateRequest.setIssueDescription("Updated Black screen after startup");
        updateRequest.setDesiredServiceDate(LocalDate.now().plusDays(5));
        updateRequest.setPaymentMethodId(paymentMethodId);
        updateRequest.setCouponCode("DISCOUNT20");

        when(repairOrderRepository.findById(sampleRepairOrder.getId())).thenReturn(Optional.of(sampleRepairOrder));
        when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Optional.of(paymentMethod));
        when(couponRepository.findByCode("DISCOUNT20")).thenReturn(Optional.of(newCoupon));

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
        assertEquals(updateRequest.getItemName(), response.getData().getItemName());
        assertEquals(newCouponId, response.getData().getCouponId());

        verify(repairOrderRepository).findById(sampleRepairOrder.getId());
        verify(paymentMethodRepository).findById(paymentMethodId);
        verify(couponRepository).findByCode("DISCOUNT20");
        // Should increment new coupon and decrement old coupon
        verify(couponRepository, times(2)).saveAndFlush(any(Coupon.class));
        verify(repairOrderRepository).save(any(RepairOrder.class));
        assertEquals(2, newCoupon.getUsageCount()); // Incremented
        assertEquals(1, coupon.getUsageCount()); // Decremented
    }

    @Test
    void updateRepairOrder_Success_RemoveCoupon() {
        // Arrange
        String repairOrderId = sampleRepairOrder.getId().toString();
        CreateAndUpdateRepairOrderRequest updateRequest = new CreateAndUpdateRepairOrderRequest();
        updateRequest.setItemName("Updated Laptop");
        updateRequest.setItemCondition("Updated Screen not working");
        updateRequest.setIssueDescription("Updated Black screen after startup");
        updateRequest.setDesiredServiceDate(LocalDate.now().plusDays(5));
        updateRequest.setPaymentMethodId(paymentMethodId);
        updateRequest.setCouponCode(null); // Remove coupon

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
                .coupon(null)
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
        assertNull(response.getData().getCouponId());

        verify(repairOrderRepository).findById(sampleRepairOrder.getId());
        verify(paymentMethodRepository).findById(paymentMethodId);
        verify(couponRepository, never()).findByCode(any());
        // Should decrement old coupon usage
        verify(couponRepository).saveAndFlush(coupon);
        verify(repairOrderRepository).save(any(RepairOrder.class));
        assertEquals(1, coupon.getUsageCount()); // Decremented from 2 to 1
    }

    @Test
    void updateRepairOrder_Success_AddCouponToOrderWithoutCoupon() {
        // Arrange
        String repairOrderId = sampleRepairOrder.getId().toString();
        RepairOrder orderWithoutCoupon = RepairOrder.builder()
                .id(sampleRepairOrder.getId())
                .customerId(customerId)
                .technicianId(technicianId)
                .status(RepairOrderStatus.PENDING_CONFIRMATION)
                .itemName("Laptop")
                .itemCondition("Screen not working")
                .issueDescription("Black screen after startup")
                .desiredServiceDate(LocalDate.now().plusDays(2))
                .paymentMethod(paymentMethod)
                .coupon(null) // No coupon initially
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(repairOrderRepository.findById(sampleRepairOrder.getId())).thenReturn(Optional.of(orderWithoutCoupon));
        when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Optional.of(paymentMethod));
        when(couponRepository.findByCode("DISCOUNT10")).thenReturn(Optional.of(coupon));

        RepairOrder updatedOrder = RepairOrder.builder()
                .id(sampleRepairOrder.getId())
                .customerId(customerId)
                .technicianId(technicianId)
                .status(RepairOrderStatus.PENDING_CONFIRMATION)
                .itemName(validRequest.getItemName())
                .itemCondition(validRequest.getItemCondition())
                .issueDescription(validRequest.getIssueDescription())
                .desiredServiceDate(validRequest.getDesiredServiceDate())
                .paymentMethod(paymentMethod)
                .coupon(coupon)
                .createdAt(orderWithoutCoupon.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
        when(repairOrderRepository.save(any(RepairOrder.class))).thenReturn(updatedOrder);

        // Act
        GenericResponse<ViewRepairOrderResponse> response = repairOrderService.updateRepairOrder(
                repairOrderId, validRequest, customer);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Repair order updated successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(couponId, response.getData().getCouponId());

        verify(repairOrderRepository).findById(sampleRepairOrder.getId());
        verify(paymentMethodRepository).findById(paymentMethodId);
        verify(couponRepository).findByCode("DISCOUNT10");
        // Should increment new coupon usage
        verify(couponRepository).saveAndFlush(coupon);
        verify(repairOrderRepository).save(any(RepairOrder.class));
        assertEquals(3, coupon.getUsageCount()); // Incremented from 2 to 3
    }

    @Test
    void updateRepairOrder_CouponOptimisticLockingFailure_ThrowsException() {
        // Arrange
        String repairOrderId = sampleRepairOrder.getId().toString();
        UUID newCouponId = UUID.randomUUID();
        Coupon newCoupon = Coupon.builder()
                .id(newCouponId)
                .code("DISCOUNT20")
                .discountAmount(20)
                .maxUsage(5)
                .usageCount(1)
                .validUntil(LocalDate.now().plusDays(30))
                .build();

        CreateAndUpdateRepairOrderRequest updateRequest = new CreateAndUpdateRepairOrderRequest();
        updateRequest.setItemName("Updated Laptop");
        updateRequest.setItemCondition("Updated condition");
        updateRequest.setIssueDescription("Updated issue");
        updateRequest.setDesiredServiceDate(LocalDate.now().plusDays(5));
        updateRequest.setPaymentMethodId(paymentMethodId);
        updateRequest.setCouponCode("DISCOUNT20");

        when(repairOrderRepository.findById(sampleRepairOrder.getId())).thenReturn(Optional.of(sampleRepairOrder));
        when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Optional.of(paymentMethod));
        when(couponRepository.findByCode("DISCOUNT20")).thenReturn(Optional.of(newCoupon));
        when(couponRepository.saveAndFlush(newCoupon)).thenThrow(new OptimisticLockingFailureException("Concurrent modification"));

        // Act & Assert
        InvalidRepairOrderStateException exception = assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.updateRepairOrder(repairOrderId, updateRequest, customer)
        );

        assertEquals("Coupon usage conflict, please try again", exception.getMessage());
        verify(repairOrderRepository).findById(sampleRepairOrder.getId());
        verify(paymentMethodRepository).findById(paymentMethodId);
        verify(couponRepository).findByCode("DISCOUNT20");
        verify(couponRepository).saveAndFlush(newCoupon);
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
    void updateRepairOrder_OrderNotFound_ThrowsException() {
        String repairOrderId = UUID.randomUUID().toString();
        when(repairOrderRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.updateRepairOrder(repairOrderId, validRequest, customer)
        );
        verify(repairOrderRepository).findById(any(UUID.class));
        verifyNoInteractions(paymentMethodRepository);
        verifyNoInteractions(couponRepository);
    }

    @Test
    void updateRepairOrder_UnauthorizedCustomer_ThrowsException() {
        String repairOrderId = sampleRepairOrder.getId().toString();
        UUID differentCustomerId = UUID.randomUUID();
        AuthenticatedUser differentCustomer = new AuthenticatedUser(
                differentCustomerId,
                "other@example.com",
                "Other",
                UserRole.CUSTOMER,
                "1234567890",
                Instant.now(),
                Instant.now(),
                "Jakarta",
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
    }

    @Test
    void updateRepairOrder_InvalidStatus_ThrowsException() {
        String repairOrderId = sampleRepairOrder.getId().toString();
        RepairOrder inProgressOrder = RepairOrder.builder()
                .id(sampleRepairOrder.getId())
                .customerId(customerId)
                .technicianId(technicianId)
                .status(RepairOrderStatus.IN_PROGRESS) // Not PENDING_CONFIRMATION
                .itemName("Laptop")
                .itemCondition("Screen not working")
                .issueDescription("Black screen after startup")
                .desiredServiceDate(LocalDate.now().plusDays(2))
                .paymentMethod(paymentMethod)
                .coupon(coupon)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(repairOrderRepository.findById(sampleRepairOrder.getId())).thenReturn(Optional.of(inProgressOrder));

        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.updateRepairOrder(repairOrderId, validRequest, customer)
        );
        verify(repairOrderRepository).findById(sampleRepairOrder.getId());
        verifyNoInteractions(paymentMethodRepository);
        verifyNoInteractions(couponRepository);
    }

    @Test
    void updateRepairOrder_InvalidPaymentMethod_ThrowsException() {
        String repairOrderId = sampleRepairOrder.getId().toString();
        when(repairOrderRepository.findById(sampleRepairOrder.getId())).thenReturn(Optional.of(sampleRepairOrder));
        when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.updateRepairOrder(repairOrderId, validRequest, customer)
        );
        verify(repairOrderRepository).findById(sampleRepairOrder.getId());
        verify(paymentMethodRepository).findById(paymentMethodId);
        verifyNoInteractions(couponRepository);
    }

    @Test
    void updateRepairOrder_DatabaseException_ThrowsException() {
        String repairOrderId = sampleRepairOrder.getId().toString();
        when(repairOrderRepository.findById(sampleRepairOrder.getId())).thenReturn(Optional.of(sampleRepairOrder));
        when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Optional.of(paymentMethod));
        when(couponRepository.findByCode("DISCOUNT10")).thenReturn(Optional.of(coupon));
        when(repairOrderRepository.save(any(RepairOrder.class))).thenThrow(mock(DataAccessException.class));

        // Act & Assert
        assertThrows(DatabaseException.class, () ->
                repairOrderService.updateRepairOrder(repairOrderId, validRequest, customer)
        );
        verify(repairOrderRepository).findById(sampleRepairOrder.getId());
        verify(paymentMethodRepository).findById(paymentMethodId);
        verify(couponRepository).findByCode("DISCOUNT10");
        verify(repairOrderRepository).save(any(RepairOrder.class));
    }

    @Test
    void updateRepairOrder_InvalidUUID_ThrowsException() {
        String invalidId = "not-a-uuid";

        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.updateRepairOrder(invalidId, validRequest, customer)
        );
        verifyNoInteractions(repairOrderRepository);
        verifyNoInteractions(paymentMethodRepository);
        verifyNoInteractions(couponRepository);
    }

    // DELETE REPAIR ORDER TESTS
    @Test
    void deleteRepairOrder_Success_WithCoupon() {
        // Arrange
        String repairOrderId = sampleRepairOrder.getId().toString();
        when(repairOrderRepository.findById(sampleRepairOrder.getId())).thenReturn(Optional.of(sampleRepairOrder));

        // Act
        GenericResponse<Void> response = repairOrderService.deleteRepairOrder(repairOrderId, customer);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Repair order deleted successfully", response.getMessage());
        assertNull(response.getData());

        verify(repairOrderRepository).findById(sampleRepairOrder.getId());
        verify(couponRepository).saveAndFlush(coupon); // Should decrement usage count
        verify(repairOrderRepository).delete(sampleRepairOrder);
        assertEquals(1, coupon.getUsageCount()); // Decremented from 2 to 1
    }

    @Test
    void deleteRepairOrder_Success_WithoutCoupon() {
        // Arrange
        String repairOrderId = sampleRepairOrder.getId().toString();
        RepairOrder orderWithoutCoupon = RepairOrder.builder()
                .id(sampleRepairOrder.getId())
                .customerId(customerId)
                .technicianId(technicianId)
                .status(RepairOrderStatus.PENDING_CONFIRMATION)
                .itemName("Laptop")
                .itemCondition("Screen not working")
                .issueDescription("Black screen after startup")
                .desiredServiceDate(LocalDate.now().plusDays(2))
                .paymentMethod(paymentMethod)
                .coupon(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(repairOrderRepository.findById(sampleRepairOrder.getId())).thenReturn(Optional.of(orderWithoutCoupon));

        // Act
        GenericResponse<Void> response = repairOrderService.deleteRepairOrder(repairOrderId, customer);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Repair order deleted successfully", response.getMessage());
        assertNull(response.getData());

        verify(repairOrderRepository).findById(sampleRepairOrder.getId());
        verify(couponRepository, never()).saveAndFlush(any(Coupon.class));
        verify(repairOrderRepository).delete(orderWithoutCoupon);
    }

    @Test
    void deleteRepairOrder_CouponOptimisticLockingFailure_ThrowsException() {
        // Arrange
        String repairOrderId = sampleRepairOrder.getId().toString();
        when(repairOrderRepository.findById(sampleRepairOrder.getId())).thenReturn(Optional.of(sampleRepairOrder));
        when(couponRepository.saveAndFlush(coupon)).thenThrow(new OptimisticLockingFailureException("Concurrent modification"));

        // Act & Assert
        InvalidRepairOrderStateException exception = assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.deleteRepairOrder(repairOrderId, customer)
        );

        assertEquals("Coupon usage conflict, please try again", exception.getMessage());
        verify(repairOrderRepository).findById(sampleRepairOrder.getId());
        verify(couponRepository).saveAndFlush(coupon);
        verify(repairOrderRepository, never()).delete(any(RepairOrder.class));
    }

    @Test
    void deleteRepairOrder_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.deleteRepairOrder(null, customer)
        );
        verifyNoInteractions(repairOrderRepository);
        verifyNoInteractions(couponRepository);
    }

    @Test
    void deleteRepairOrder_NullCustomer_ThrowsException() {
        String repairOrderId = sampleRepairOrder.getId().toString();

        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.deleteRepairOrder(repairOrderId, null)
        );
        verifyNoInteractions(repairOrderRepository);
        verifyNoInteractions(couponRepository);
    }

    @Test
    void deleteRepairOrder_OrderNotFound_ThrowsException() {
        String repairOrderId = UUID.randomUUID().toString();
        when(repairOrderRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.deleteRepairOrder(repairOrderId, customer)
        );
        verify(repairOrderRepository).findById(any(UUID.class));
        verifyNoInteractions(couponRepository);
    }

    @Test
    void deleteRepairOrder_UnauthorizedCustomer_ThrowsException() {
        String repairOrderId = sampleRepairOrder.getId().toString();
        UUID differentCustomerId = UUID.randomUUID();
        AuthenticatedUser differentCustomer = new AuthenticatedUser(
                differentCustomerId,
                "other@example.com",
                "Other",
                UserRole.CUSTOMER,
                "1234567890",
                Instant.now(),
                Instant.now(),
                "Jakarta",
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
        verifyNoInteractions(couponRepository);
    }

    @Test
    void deleteRepairOrder_InvalidStatus_ThrowsException() {
        String repairOrderId = sampleRepairOrder.getId().toString();
        RepairOrder completedOrder = RepairOrder.builder()
                .id(sampleRepairOrder.getId())
                .customerId(customerId)
                .technicianId(technicianId)
                .status(RepairOrderStatus.COMPLETED) // Not PENDING_CONFIRMATION
                .itemName("Laptop")
                .itemCondition("Screen not working")
                .issueDescription("Black screen after startup")
                .desiredServiceDate(LocalDate.now().plusDays(2))
                .paymentMethod(paymentMethod)
                .coupon(coupon)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(repairOrderRepository.findById(sampleRepairOrder.getId())).thenReturn(Optional.of(completedOrder));

        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.deleteRepairOrder(repairOrderId, customer)
        );
        verify(repairOrderRepository).findById(sampleRepairOrder.getId());
        verifyNoInteractions(couponRepository);
    }

    @Test
    void deleteRepairOrder_DatabaseException_ThrowsException() {
        String repairOrderId = sampleRepairOrder.getId().toString();
        when(repairOrderRepository.findById(sampleRepairOrder.getId())).thenReturn(Optional.of(sampleRepairOrder));
        doThrow(mock(DataAccessException.class)).when(repairOrderRepository).delete(sampleRepairOrder);

        // Act & Assert
        assertThrows(DatabaseException.class, () ->
                repairOrderService.deleteRepairOrder(repairOrderId, customer)
        );
        verify(repairOrderRepository).findById(sampleRepairOrder.getId());
        verify(couponRepository).saveAndFlush(coupon);
        verify(repairOrderRepository).delete(sampleRepairOrder);
    }

    @Test
    void deleteRepairOrder_InvalidUUID_ThrowsException() {
        String invalidId = "not-a-uuid";

        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.deleteRepairOrder(invalidId, customer)
        );
        verifyNoInteractions(repairOrderRepository);
        verifyNoInteractions(couponRepository);
    }

    // EDGE CASES AND ADDITIONAL SCENARIOS
    @Test
    void createRepairOrder_InvalidTechnicianId_ThrowsException() {
        // Arrange
        UserIdentity invalidTechnicianIdentity = UserIdentity.newBuilder()
                .setId("invalid-uuid") // Invalid UUID format
                .build();
        UserData invalidTechnicianData = UserData.newBuilder()
                .setIdentity(invalidTechnicianIdentity)
                .build();
        GetRandomTechnicianResponse invalidResponse = GetRandomTechnicianResponse.newBuilder()
                .setTechnician(invalidTechnicianData)
                .build();

        when(userServiceGrpcClient.getRandomTechnician()).thenReturn(invalidResponse);
        when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Optional.of(paymentMethod));
        when(couponRepository.findByCode("DISCOUNT10")).thenReturn(Optional.of(coupon));

        // Act & Assert
        InvalidRepairOrderStateException exception = assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.createRepairOrder(validRequest, customer)
        );

        assertEquals("Invalid technician ID or malformed data", exception.getMessage());
        verify(userServiceGrpcClient).getRandomTechnician();
        verify(paymentMethodRepository).findById(paymentMethodId);
        verify(couponRepository).findByCode("DISCOUNT10");
        verify(couponRepository).saveAndFlush(coupon);
        verify(repairOrderRepository, never()).save(any(RepairOrder.class));
    }

    @Test
    void getViewRepairOrderResponse_NullPaymentMethod_HandlesGracefully() {
        // Arrange
        RepairOrder orderWithoutPaymentMethod = RepairOrder.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .technicianId(technicianId)
                .status(RepairOrderStatus.PENDING_CONFIRMATION)
                .itemName("Laptop")
                .itemCondition("Screen not working")
                .issueDescription("Black screen after startup")
                .desiredServiceDate(LocalDate.now().plusDays(2))
                .paymentMethod(null)
                .coupon(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(repairOrderRepository.findByCustomerId(customerId)).thenReturn(List.of(orderWithoutPaymentMethod));

        // Act
        GenericResponse<List<ViewRepairOrderResponse>> response = repairOrderService.getRepairOrders(customer);

        // Assert
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
        ViewRepairOrderResponse responseData = response.getData().getFirst();
        assertNull(responseData.getPaymentMethodId());
        assertNull(responseData.getCouponId());
        verify(repairOrderRepository).findByCustomerId(customerId);
    }

    @Test
    void updateRepairOrder_CouponUsageCountAtZero_HandlesGracefully() {
        // Arrange - Coupon with 0 usage count
        Coupon zeroUsageCoupon = Coupon.builder()
                .id(couponId)
                .code("DISCOUNT10")
                .discountAmount(10)
                .maxUsage(5)
                .usageCount(0) // Zero usage count
                .validUntil(LocalDate.now().plusDays(30))
                .build();

        RepairOrder orderWithZeroUsageCoupon = RepairOrder.builder()
                .id(sampleRepairOrder.getId())
                .customerId(customerId)
                .technicianId(technicianId)
                .status(RepairOrderStatus.PENDING_CONFIRMATION)
                .itemName("Laptop")
                .itemCondition("Screen not working")
                .issueDescription("Black screen after startup")
                .desiredServiceDate(LocalDate.now().plusDays(2))
                .paymentMethod(paymentMethod)
                .coupon(zeroUsageCoupon)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        String repairOrderId = sampleRepairOrder.getId().toString();
        CreateAndUpdateRepairOrderRequest updateRequest = new CreateAndUpdateRepairOrderRequest();
        updateRequest.setItemName("Updated Laptop");
        updateRequest.setItemCondition("Updated condition");
        updateRequest.setIssueDescription("Updated issue");
        updateRequest.setDesiredServiceDate(LocalDate.now().plusDays(5));
        updateRequest.setPaymentMethodId(paymentMethodId);
        updateRequest.setCouponCode(null); // Remove coupon

        when(repairOrderRepository.findById(sampleRepairOrder.getId())).thenReturn(Optional.of(orderWithZeroUsageCoupon));
        when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Optional.of(paymentMethod));
        when(repairOrderRepository.save(any(RepairOrder.class))).thenReturn(orderWithZeroUsageCoupon);

        // Act
        GenericResponse<ViewRepairOrderResponse> response = repairOrderService.updateRepairOrder(
                repairOrderId, updateRequest, customer);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Repair order updated successfully", response.getMessage());

        // Verify coupon usage count remains at 0 (Math.max(0, 0-1) = 0)
        verify(couponRepository).saveAndFlush(zeroUsageCoupon);
        assertEquals(0, zeroUsageCoupon.getUsageCount());
    }

    @Test
    void deleteRepairOrder_CouponUsageCountAtZero_HandlesGracefully() {
        // Arrange - Coupon with 0 usage count
        Coupon zeroUsageCoupon = Coupon.builder()
                .id(couponId)
                .code("DISCOUNT10")
                .discountAmount(10)
                .maxUsage(5)
                .usageCount(0) // Zero usage count
                .validUntil(LocalDate.now().plusDays(30))
                .build();

        RepairOrder orderWithZeroUsageCoupon = RepairOrder.builder()
                .id(sampleRepairOrder.getId())
                .customerId(customerId)
                .technicianId(technicianId)
                .status(RepairOrderStatus.PENDING_CONFIRMATION)
                .itemName("Laptop")
                .itemCondition("Screen not working")
                .issueDescription("Black screen after startup")
                .desiredServiceDate(LocalDate.now().plusDays(2))
                .paymentMethod(paymentMethod)
                .coupon(zeroUsageCoupon)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        String repairOrderId = sampleRepairOrder.getId().toString();
        when(repairOrderRepository.findById(sampleRepairOrder.getId())).thenReturn(Optional.of(orderWithZeroUsageCoupon));

        // Act
        GenericResponse<Void> response = repairOrderService.deleteRepairOrder(repairOrderId, customer);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Repair order deleted successfully", response.getMessage());

        // Verify coupon usage count remains at 0 (Math.max(0, 0-1) = 0)
        verify(couponRepository).saveAndFlush(zeroUsageCoupon);
        assertEquals(0, zeroUsageCoupon.getUsageCount());
        verify(repairOrderRepository).delete(orderWithZeroUsageCoupon);
    }
}