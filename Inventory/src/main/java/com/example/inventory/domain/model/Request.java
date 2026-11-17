// src/main/java/com/example/inventory/domain/model/Request.java
// (既存コードの user フィールドを employee に変更)
package com.example.inventory.domain.model;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "requests")
@Getter @Setter @NoArgsConstructor
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestType requestType;

    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime dueDate;
    private LocalDateTime returnedAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne
    @JoinColumn(name = "equipment_unit_id")
    private EquipmentUnit equipmentUnit;
    
    @Column(length = 1000)
    private String requestNote;
    
    @Column(length = 1000)
    private String rejectNote;
    
    @ManyToOne
    @JoinColumn(name = "request_reason_id")
    private RequestReason requestReason;
    
    @ManyToOne
    @JoinColumn(name = "reject_reason_id")
    private RejectReason rejectReason;

    @Transient
    public List<RequestDetailView> getDetails() {
        return List.of(new RequestDetailView(this));
    }

    public static class RequestDetailView {
        private final Request request;
        public RequestDetailView(Request request) { this.request = request; }
        public EquipmentUnit getEquipmentUnit() { return request.requestType == RequestType.BORROW ? request.equipmentUnit : null; }
        public Item getItem() { return request.requestType == RequestType.CONSUME ? request.item : null; }
        public Integer getQuantity() { return request.quantity; }
    }
}