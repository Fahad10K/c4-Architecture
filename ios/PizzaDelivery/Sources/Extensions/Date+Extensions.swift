import Foundation

extension String {
    func toDate() -> Date? {
        let formatters: [DateFormatter] = {
            let iso = DateFormatter()
            iso.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
            
            let iso2 = DateFormatter()
            iso2.dateFormat = "yyyy-MM-dd'T'HH:mm:ssZ"
            
            let simple = DateFormatter()
            simple.dateFormat = "yyyy-MM-dd HH:mm:ss"
            
            return [iso, iso2, simple]
        }()
        
        for formatter in formatters {
            if let date = formatter.date(from: self) {
                return date
            }
        }
        return nil
    }
    
    func formattedDate() -> String {
        guard let date = self.toDate() else { return self }
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        return formatter.string(from: date)
    }
    
    func relativeTime() -> String {
        guard let date = self.toDate() else { return self }
        let formatter = RelativeDateTimeFormatter()
        formatter.unitsStyle = .abbreviated
        return formatter.localizedString(for: date, relativeTo: Date())
    }
}

extension Double {
    var currencyFormatted: String {
        String(format: "$%.2f", self)
    }
}
