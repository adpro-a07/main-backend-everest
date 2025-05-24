package id.ac.ui.cs.advprog.everest.modules.repairorder.service;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.common.service.UserServiceGrpcClient;
import id.ac.ui.cs.advprog.everest.modules.coupon.model.Coupon;
import id.ac.ui.cs.advprog.everest.modules.coupon.repository.CouponRepository;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.model.PaymentMethod;
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
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserData;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class RepairOrderServiceImpl implements RepairOrderService {

    private final UserServiceGrpcClient userServiceGrpcClient;
    private final RepairOrderRepository repairOrderRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final CouponRepository couponRepository;

    // --- Constants for reused messages ---
    private static final String ERR_NULL_REQUEST_OR_CUSTOMER = "Request or customer cannot be null";
    private static final String ERR_INVALID_PAYMENT_METHOD = "Invalid payment method";
    private static final String ERR_INVALID_TECHNICIAN_ID = "Invalid technician ID or malformed data";
    private static final String ERR_SAVE_FAILED = "Failed to save repair order";
    private static final String MSG_CREATE_SUCCESS = "Repair order created successfully";

    private static final String ERR_NULL_CUSTOMER = "Customer cannot be null";
    private static final String ERR_RETRIEVE_FAILED = "Failed to retrieve repair orders";
    private static final String MSG_RETRIEVE_SUCCESS = "Repair orders retrieved successfully";

    private static final String ERR_NULL_ID_OR_CUSTOMER = "Repair order ID or customer cannot be null";
    private static final String ERR_ORDER_NOT_FOUND = "Repair order not found";
    private static final String ERR_UNAUTHORIZED_VIEW = "You are not authorized to view this repair order";
    private static final String ERR_INVALID_ORDER_ID = "Invalid repair order ID";
    private static final String ERR_RETRIEVE_ORDER_FAILED = "Failed to retrieve repair order";
    private static final String MSG_RETRIEVE_ORDER_SUCCESS = "Repair order retrieved successfully";

    private static final String ERR_NULL_ID_REQUEST_CUSTOMER = "Repair order ID, request, or customer cannot be null";
    private static final String ERR_UNAUTHORIZED_UPDATE = "You are not authorized to update this repair order";
    private static final String ERR_CANNOT_UPDATE = "Repair order cannot be updated";
    private static final String ERR_INVALID_DATA = "Invalid data provided";
    private static final String ERR_UPDATE_FAILED = "Failed to update repair order";
    private static final String MSG_UPDATE_SUCCESS = "Repair order updated successfully";

    private static final String ERR_UNAUTHORIZED_DELETE = "You are not authorized to delete this repair order";
    private static final String ERR_CANNOT_DELETE = "Repair order cannot be deleted";
    private static final String ERR_DELETE_FAILED = "Failed to delete repair order";
    private static final String MSG_DELETE_SUCCESS = "Repair order deleted successfully";


    public RepairOrderServiceImpl(
            UserServiceGrpcClient userServiceGrpcClient,
            RepairOrderRepository repairOrderRepository,
            PaymentMethodRepository paymentMethodRepository,
            CouponRepository couponRepository
    ) {
        this.userServiceGrpcClient = userServiceGrpcClient;
        this.repairOrderRepository = repairOrderRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.couponRepository = couponRepository;
    }

    @Override
    @Transactional
    public GenericResponse<ViewRepairOrderResponse> createRepairOrder(CreateAndUpdateRepairOrderRequest request,
                                                                      AuthenticatedUser customer) {
        if (request == null || customer == null) {
            throw new InvalidRepairOrderStateException(ERR_NULL_REQUEST_OR_CUSTOMER);
        }

        try {
            GetRandomTechnicianResponse response = userServiceGrpcClient.getRandomTechnician();

            if (!response.hasTechnician()) {
                throw new TechnicianUnavailableException();
            }

            UserData technician = response.getTechnician();

            PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPaymentMethodId())
                    .orElseThrow(() -> new InvalidRepairOrderStateException(ERR_INVALID_PAYMENT_METHOD));

            Coupon coupon = null;
            if (request.getCouponCode() != null) {
                coupon = getAndValidateCoupon(request);

                try {
                    coupon.setUsageCount(coupon.getUsageCount() + 1);
                    couponRepository.saveAndFlush(coupon);
                } catch (OptimisticLockingFailureException e) {
                    throw new InvalidRepairOrderStateException("Coupon usage conflict, please try again", e);
                }
            }

            RepairOrder repairOrder = RepairOrder.builder()
                    .customerId(customer.id())
                    .technicianId(UUID.fromString(technician.getIdentity().getId()))
                    .status(RepairOrderStatus.PENDING_CONFIRMATION)
                    .itemName(request.getItemName())
                    .itemCondition(request.getItemCondition())
                    .issueDescription(request.getIssueDescription())
                    .desiredServiceDate(request.getDesiredServiceDate())
                    .paymentMethod(paymentMethod)
                    .coupon(coupon)
                    .build();

            RepairOrder savedRepairOrder = repairOrderRepository.save(repairOrder);

            ViewRepairOrderResponse responseView = getViewRepairOrderResponse(savedRepairOrder);

            return new GenericResponse<>(true, MSG_CREATE_SUCCESS, responseView);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new InvalidRepairOrderStateException(ERR_INVALID_TECHNICIAN_ID, ex);
        } catch (DataAccessException ex) {
            throw new DatabaseException(ERR_SAVE_FAILED, ex);
        }
    }

    @NotNull
    private Coupon getAndValidateCoupon(CreateAndUpdateRepairOrderRequest request) {
        Coupon coupon;
        coupon = couponRepository.findByCode(request.getCouponCode())
                .orElseThrow(() -> new InvalidRepairOrderStateException("Coupon not found"));

        if (coupon.getValidUntil() != null && coupon.getValidUntil().isBefore(LocalDate.now())) {
            throw new InvalidRepairOrderStateException("Coupon has expired");
        }

        if (coupon.getUsageCount() >= coupon.getMaxUsage()) {
            throw new InvalidRepairOrderStateException("Coupon has reached its maximum usage limit");
        }
        return coupon;
    }

    @Override
    public GenericResponse<List<ViewRepairOrderResponse>> getRepairOrders(AuthenticatedUser customer) {
        if (customer == null) {
            throw new InvalidRepairOrderStateException(ERR_NULL_CUSTOMER);
        }

        try {
            List<RepairOrder> repairOrders = repairOrderRepository.findByCustomerId(customer.id());

            List<ViewRepairOrderResponse> responseList = repairOrders.stream()
                    .map(this::getViewRepairOrderResponse)
                    .toList();

            return new GenericResponse<>(true, MSG_RETRIEVE_SUCCESS, responseList);

        } catch (DataAccessException ex) {
            throw new DatabaseException(ERR_RETRIEVE_FAILED, ex);
        }
    }

    @Override
    public GenericResponse<ViewRepairOrderResponse> getRepairOrderById(String repairOrderId, AuthenticatedUser customer) {
        if (repairOrderId == null || customer == null) {
            throw new InvalidRepairOrderStateException(ERR_NULL_ID_OR_CUSTOMER);
        }

        try {
            RepairOrder repairOrder = repairOrderRepository.findById(UUID.fromString(repairOrderId))
                    .orElseThrow(() -> new InvalidRepairOrderStateException(ERR_ORDER_NOT_FOUND));

            if (!repairOrder.getCustomerId().equals(customer.id())) {
                throw new InvalidRepairOrderStateException(ERR_UNAUTHORIZED_VIEW);
            }

            ViewRepairOrderResponse responseView = getViewRepairOrderResponse(repairOrder);

            return new GenericResponse<>(true, MSG_RETRIEVE_ORDER_SUCCESS, responseView);

        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new InvalidRepairOrderStateException(ERR_INVALID_ORDER_ID, ex);
        } catch (DataAccessException ex) {
            throw new DatabaseException(ERR_RETRIEVE_ORDER_FAILED, ex);
        }
    }

    @Override
    @Transactional
    public GenericResponse<ViewRepairOrderResponse> updateRepairOrder(
            String repairOrderId,
            CreateAndUpdateRepairOrderRequest request,
            AuthenticatedUser customer
    ) {
        if (repairOrderId == null || request == null || customer == null) {
            throw new InvalidRepairOrderStateException(ERR_NULL_ID_REQUEST_CUSTOMER);
        }

        try {
            RepairOrder repairOrder = getRepairOrderByIdAndValidateState(repairOrderId, customer, ERR_UNAUTHORIZED_UPDATE, ERR_CANNOT_UPDATE);

            PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPaymentMethodId())
                    .orElseThrow(() -> new InvalidRepairOrderStateException(ERR_INVALID_PAYMENT_METHOD));

            Coupon oldCoupon = repairOrder.getCoupon();
            Coupon newCoupon = null;

            if (request.getCouponCode() != null) {
                newCoupon = getAndValidateCoupon(request);

                if (oldCoupon == null || !oldCoupon.getId().equals(newCoupon.getId())) {
                    try {
                        newCoupon.setUsageCount(newCoupon.getUsageCount() + 1);
                        couponRepository.saveAndFlush(newCoupon);

                        if (oldCoupon != null) {
                            int newUsage = Math.max(0, oldCoupon.getUsageCount() - 1);
                            oldCoupon.setUsageCount(newUsage);
                            couponRepository.saveAndFlush(oldCoupon);
                        }
                    } catch (OptimisticLockingFailureException e) {
                        throw new InvalidRepairOrderStateException("Coupon usage conflict, please try again", e);
                    }
                }
            } else if (oldCoupon != null) {
                // Coupon removed
                int newUsage = Math.max(0, oldCoupon.getUsageCount() - 1);
                oldCoupon.setUsageCount(newUsage);
                couponRepository.saveAndFlush(oldCoupon);
            }

            repairOrder.setItemName(request.getItemName());
            repairOrder.setItemCondition(request.getItemCondition());
            repairOrder.setIssueDescription(request.getIssueDescription());
            repairOrder.setDesiredServiceDate(request.getDesiredServiceDate());
            repairOrder.setPaymentMethod(paymentMethod);
            repairOrder.setCoupon(newCoupon);

            RepairOrder updatedRepairOrder = repairOrderRepository.save(repairOrder);

            ViewRepairOrderResponse responseView = getViewRepairOrderResponse(updatedRepairOrder);

            return new GenericResponse<>(true, MSG_UPDATE_SUCCESS, responseView);

        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new InvalidRepairOrderStateException(ERR_INVALID_DATA, ex);
        } catch (DataAccessException ex) {
            throw new DatabaseException(ERR_UPDATE_FAILED, ex);
        }
    }

    private RepairOrder getRepairOrderByIdAndValidateState(String repairOrderId, AuthenticatedUser customer, String errUnauthorizedUpdate, String errCannotUpdate) {
        RepairOrder repairOrder = repairOrderRepository.findById(UUID.fromString(repairOrderId))
                .orElseThrow(() -> new InvalidRepairOrderStateException(ERR_ORDER_NOT_FOUND));

        if (!repairOrder.getCustomerId().equals(customer.id())) {
            throw new InvalidRepairOrderStateException(errUnauthorizedUpdate);
        }

        if (repairOrder.getStatus() != RepairOrderStatus.PENDING_CONFIRMATION) {
            throw new InvalidRepairOrderStateException(errCannotUpdate);
        }
        return repairOrder;
    }

    @Override
    @Transactional
    public GenericResponse<Void> deleteRepairOrder(String repairOrderId, AuthenticatedUser customer) {
        if (repairOrderId == null || customer == null) {
            throw new InvalidRepairOrderStateException(ERR_NULL_ID_OR_CUSTOMER);
        }

        try {
            RepairOrder repairOrder = getRepairOrderByIdAndValidateState(repairOrderId, customer, ERR_UNAUTHORIZED_DELETE, ERR_CANNOT_DELETE);

            if (repairOrder.getCoupon() != null) {
                Coupon coupon = repairOrder.getCoupon();
                int newUsage = Math.max(0, coupon.getUsageCount() - 1);
                coupon.setUsageCount(newUsage);
                try {
                    couponRepository.saveAndFlush(coupon);
                } catch (OptimisticLockingFailureException e) {
                    throw new InvalidRepairOrderStateException("Coupon usage conflict, please try again", e);
                }
            }

            repairOrderRepository.delete(repairOrder);

            return new GenericResponse<>(true, MSG_DELETE_SUCCESS, null);

        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new InvalidRepairOrderStateException(ERR_INVALID_DATA, ex);
        } catch (DataAccessException ex) {
            throw new DatabaseException(ERR_DELETE_FAILED, ex);
        }
    }

    private ViewRepairOrderResponse getViewRepairOrderResponse(RepairOrder repairOrder) {
        return ViewRepairOrderResponse.builder()
                .id(repairOrder.getId())
                .customerId(repairOrder.getCustomerId())
                .technicianId(repairOrder.getTechnicianId())
                .status(repairOrder.getStatus())
                .itemName(repairOrder.getItemName())
                .itemCondition(repairOrder.getItemCondition())
                .issueDescription(repairOrder.getIssueDescription())
                .desiredServiceDate(repairOrder.getDesiredServiceDate())
                .paymentMethodId(repairOrder.getPaymentMethod() != null ? repairOrder.getPaymentMethod().getId() : null)
                .couponId(repairOrder.getCoupon() != null ? repairOrder.getCoupon().getId() : null)
                .createdAt(repairOrder.getCreatedAt())
                .updatedAt(repairOrder.getUpdatedAt())
                .build();
    }
}
