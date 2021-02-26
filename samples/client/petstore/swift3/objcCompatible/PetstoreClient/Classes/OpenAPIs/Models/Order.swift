//
// Order.swift
//
// Generated by openapi-generator
// https://openapi-generator.tech
//

import Foundation


open class Order: JSONEncodable {

    public enum Status: String { 
        case placed = "placed"
        case approved = "approved"
        case delivered = "delivered"
    }
    public var id: Int64?
    public var idNum: NSNumber? {
        get {
            return id.map({ return NSNumber(value: $0) })
        }
    }
    public var petId: Int64?
    public var petIdNum: NSNumber? {
        get {
            return petId.map({ return NSNumber(value: $0) })
        }
    }
    public var quantity: Int32?
    public var quantityNum: NSNumber? {
        get {
            return quantity.map({ return NSNumber(value: $0) })
        }
    }
    public var shipDate: Date?
    /** Order Status */
    public var status: Status?
    public var complete: Bool?
    public var completeNum: NSNumber? {
        get {
            return complete.map({ return NSNumber(value: $0) })
        }
    }

    public init() {}

    // MARK: JSONEncodable
    open func encodeToJSON() -> Any {
        var nillableDictionary = [String:Any?]()
        nillableDictionary["id"] = self.id?.encodeToJSON()
        nillableDictionary["petId"] = self.petId?.encodeToJSON()
        nillableDictionary["quantity"] = self.quantity?.encodeToJSON()
        nillableDictionary["shipDate"] = self.shipDate?.encodeToJSON()
        nillableDictionary["status"] = self.status?.rawValue
        nillableDictionary["complete"] = self.complete

        let dictionary: [String:Any] = APIHelper.rejectNil(nillableDictionary) ?? [:]
        return dictionary
    }
}

