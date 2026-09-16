package com.example.data.model

enum class UserRole(val displayName: String) {
    OWNER("Store Owner"),
    ADMIN("Admin"),
    MANAGER("Shop Manager"),
    EMPLOYEE("Employee")
}

enum class Department(val displayName: String) {
    SALES("Sales & Counter"),
    BILLING("Cashier & Billing"),
    INVENTORY("Store & Inventory"),
    DELIVERY("Delivery & Logistics"),
    CLEANING("Support & Cleaning"),
    GENERAL("General")
}

enum class EmploymentType(val displayName: String) {
    FULL_TIME("Full Time"),
    PART_TIME("Part Time"),
    TEMPORARY("Temporary"),
    INTERN("Intern")
}

enum class PaymentMethod(val displayName: String) {
    CASH("Cash"),
    UPI("UPI / Google Pay / PhonePe"),
    BANK_TRANSFER("Bank Transfer (NEFT/IMPS)"),
    CHEQUE("Cheque")
}

enum class AttendanceStatus(val displayName: String) {
    PRESENT("Present"),
    ABSENT("Absent"),
    HALF_DAY("Half Day"),
    LEAVE("On Leave"),
    HOLIDAY("Holiday"),
    WORK_FROM_HOME("Work From Home"),
    LATE("Late Arrival"),
    MISSING("Attendance Missing")
}

enum class LeaveType(val displayName: String) {
    CASUAL("Casual Leave"),
    SICK("Sick Leave"),
    PAID("Paid Leave"),
    UNPAID("Unpaid Leave"),
    EMERGENCY("Emergency Leave"),
    HALF_DAY("Half Day Leave")
}

enum class LeaveStatus(val displayName: String) {
    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected")
}

enum class PayrollStatus(val displayName: String) {
    PENDING("Pending"),
    PAID("Paid")
}

enum class AdvanceStatus(val displayName: String) {
    ACTIVE("Active"),
    REPAID("Repaid")
}

enum class OvertimeStatus(val displayName: String) {
    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected")
}

enum class TaskPriority(val displayName: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    URGENT("Urgent")
}

enum class TaskStatus(val displayName: String) {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed")
}

enum class DocumentType(val displayName: String) {
    AADHAAR("Aadhaar Card"),
    PAN("PAN Card"),
    ADDRESS_PROOF("Address Proof"),
    CONTRACT("Employment Agreement"),
    JOINING_DOC("Joining Document"),
    OTHER("Other Document")
}
