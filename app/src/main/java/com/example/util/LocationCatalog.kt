package com.example.util

data class CountryLocationSpec(
    val name: String,
    val isoCode: String,
    val flagEmoji: String,
    val dialCode: String,
    val defaultCurrency: String,
    val regionLabel: String,
    val subRegionLabel: String,
    val regions: Map<String, List<String>>
)

object LocationCatalog {

    val countries: List<CountryLocationSpec> = listOf(
        CountryLocationSpec(
            name = "Bangladesh",
            isoCode = "BD",
            flagEmoji = "🇧🇩",
            dialCode = "+880",
            defaultCurrency = "BDT",
            regionLabel = "Zila (District)",
            subRegionLabel = "Upazila / Thana",
            regions = linkedMapOf(
                "Dhaka" to listOf("Dhanmondi", "Gulshan", "Mirpur", "Uttara", "Savar", "Dhamrai", "Keraniganj", "Nawabganj", "Dohar"),
                "Chattogram" to listOf("Kotwali", "Pahartali", "Hathazari", "Raozan", "Sitakunda", "Patiya", "Mirsharai", "Anwara", "Cox's Bazar Link"),
                "Gazipur" to listOf("Gazipur Sadar", "Kaliakair", "Kaliganj", "Kapasia", "Sreepur", "Tongi"),
                "Narayanganj" to listOf("Narayanganj Sadar", "Bandar", "Rupganj", "Sonargaon", "Araihazar"),
                "Sylhet" to listOf("Sylhet Sadar", "Beanibazar", "Bishwanath", "Companiganj", "Fenchuganj", "Golapganj", "Zakiganj"),
                "Cumilla" to listOf("Cumilla Sadar", "Barura", "Brahmanpara", "Chandina", "Chauddagram", "Daudkandi", "Laksam", "Muradnagar"),
                "Rajshahi" to listOf("Rajshahi Sadar", "Bagha", "Bagmara", "Charghat", "Durgapur", "Godagari", "Paba", "Puthia"),
                "Khulna" to listOf("Khulna Sadar", "Batiaghata", "Dacope", "Dumuria", "Koyra", "Paikgachha", "Phultala", "Rupsa"),
                "Barishal" to listOf("Barishal Sadar", "Agailjhara", "Babuganj", "Bakerganj", "Banaripara", "Gaurnadi", "Mehendiganj"),
                "Rangpur" to listOf("Rangpur Sadar", "Badarganj", "Gangachhara", "Kaunia", "Mithapukur", "Pirgachha", "Pirganj"),
                "Mymensingh" to listOf("Mymensingh Sadar", "Bhaluka", "Fulbaria", "Gaffargaon", "Ishwarganj", "Muktagachha", "Trishal"),
                "Cox's Bazar" to listOf("Cox's Bazar Sadar", "Chakaria", "Teknaf", "Ukhiya", "Ramu", "Maheshkhali"),
                "Bogura" to listOf("Bogura Sadar", "Adamdighi", "Dhunat", "Gabtali", "Nandigram", "Sariakandi", "Sherpur"),
                "Jessore" to listOf("Jessore Sadar", "Abhaynagar", "Bagherpara", "Chaugachha", "Jhikargachha", "Keshabpur", "Sharsha"),
                "Tangail" to listOf("Tangail Sadar", "Basail", "Bhuapur", "Ghatail", "Kalihati", "Madhupur", "Mirzapur", "Sakhipur"),
                "Noakhali" to listOf("Noakhali Sadar", "Begumganj", "Chatkhil", "Companiganj", "Hatiya", "Senbagh", "Sonaimuri"),
                "Feni" to listOf("Feni Sadar", "Chhagalnaiya", "Daganbhuiyan", "Parshuram", "Sonagazi")
            )
        ),
        CountryLocationSpec(
            name = "United States",
            isoCode = "US",
            flagEmoji = "🇺🇸",
            dialCode = "+1",
            defaultCurrency = "USD",
            regionLabel = "State",
            subRegionLabel = "City / Borough",
            regions = linkedMapOf(
                "New York" to listOf("New York City", "Brooklyn", "Queens", "Buffalo", "Albany", "Rochester"),
                "California" to listOf("Los Angeles", "San Francisco", "San Diego", "San Jose", "Sacramento"),
                "Texas" to listOf("Houston", "Dallas", "Austin", "San Antonio", "Fort Worth"),
                "Florida" to listOf("Miami", "Orlando", "Tampa", "Jacksonville"),
                "New Jersey" to listOf("Newark", "Jersey City", "Paterson", "Edison"),
                "Michigan" to listOf("Detroit", "Hamtramck", "Ann Arbor", "Grand Rapids")
            )
        ),
        CountryLocationSpec(
            name = "United Kingdom",
            isoCode = "GB",
            flagEmoji = "🇬🇧",
            dialCode = "+44",
            defaultCurrency = "GBP",
            regionLabel = "County / Region",
            subRegionLabel = "City / Borough",
            regions = linkedMapOf(
                "Greater London" to listOf("Tower Hamlets", "Camden", "Westminster", "Newham", "Greenwich", "Croydon"),
                "West Midlands" to listOf("Birmingham", "Coventry", "Wolverhampton", "Solihull"),
                "Greater Manchester" to listOf("Manchester", "Oldham", "Salford", "Bolton"),
                "Scotland" to listOf("Edinburgh", "Glasgow", "Aberdeen", "Dundee"),
                "Wales" to listOf("Cardiff", "Swansea", "Newport")
            )
        ),
        CountryLocationSpec(
            name = "United Arab Emirates",
            isoCode = "AE",
            flagEmoji = "🇦🇪",
            dialCode = "+971",
            defaultCurrency = "USD",
            regionLabel = "Emirate",
            subRegionLabel = "District / Area",
            regions = linkedMapOf(
                "Dubai" to listOf("Deira", "Bur Dubai", "Downtown Dubai", "Dubai Marina", "Jumeirah", "Al Quoz"),
                "Abu Dhabi" to listOf("Abu Dhabi City", "Al Ain", "Mussafah", "Khalifa City"),
                "Sharjah" to listOf("Al Majaz", "Al Nahda", "Rolla", "Muwaileh"),
                "Ajman" to listOf("Ajman Downtown", "Al Nuaimia", "Al Jurf")
            )
        ),
        CountryLocationSpec(
            name = "Saudi Arabia",
            isoCode = "SA",
            flagEmoji = "🇸🇦",
            dialCode = "+966",
            defaultCurrency = "USD",
            regionLabel = "Province",
            subRegionLabel = "City / Governorate",
            regions = linkedMapOf(
                "Riyadh" to listOf("Riyadh City", "Al Kharj", "Diriyah", "Al Majma'ah"),
                "Makkah" to listOf("Jeddah", "Makkah", "Taif", "Rabigh"),
                "Eastern Province" to listOf("Dammam", "Khobar", "Dhahran", "Jubail", "Al Ahsa"),
                "Madinah" to listOf("Madinah", "Yanbu")
            )
        ),
        CountryLocationSpec(
            name = "Malaysia",
            isoCode = "MY",
            flagEmoji = "🇲🇾",
            dialCode = "+60",
            defaultCurrency = "USD",
            regionLabel = "State",
            subRegionLabel = "District / City",
            regions = linkedMapOf(
                "Kuala Lumpur" to listOf("Bukit Bintang", "Cheras", "Kepong", "Setapak", "Sentul"),
                "Selangor" to listOf("Petaling Jaya", "Shah Alam", "Subang Jaya", "Klang", "Kajang"),
                "Penang" to listOf("George Town", "Butterworth", "Bayan Lepas"),
                "Johor" to listOf("Johor Bahru", "Batu Pahat", "Muar")
            )
        ),
        CountryLocationSpec(
            name = "Singapore",
            isoCode = "SG",
            flagEmoji = "🇸🇬",
            dialCode = "+65",
            defaultCurrency = "USD",
            regionLabel = "Region",
            subRegionLabel = "Planning Area",
            regions = linkedMapOf(
                "Central" to listOf("Orchard", "Marina Bay", "Little India", "Bishan", "Toa Payoh"),
                "East" to listOf("Tampines", "Bedok", "Pasir Ris", "Changi"),
                "West" to listOf("Jurong East", "Clementi", "Bukit Batok"),
                "North" to listOf("Woodlands", "Yishun", "Sembawang")
            )
        ),
        CountryLocationSpec(
            name = "India",
            isoCode = "IN",
            flagEmoji = "🇮🇳",
            dialCode = "+91",
            defaultCurrency = "USD",
            regionLabel = "State / UT",
            subRegionLabel = "District / City",
            regions = linkedMapOf(
                "West Bengal" to listOf("Kolkata", "Howrah", "Siliguri", "Durgapur", "Asansol"),
                "Maharashtra" to listOf("Mumbai", "Pune", "Nagpur", "Thane"),
                "Delhi" to listOf("New Delhi", "South Delhi", "Dwarka", "Rohini"),
                "Karnataka" to listOf("Bengaluru", "Mysuru", "Mangaluru"),
                "Tamil Nadu" to listOf("Chennai", "Coimbatore", "Madurai")
            )
        ),
        CountryLocationSpec(
            name = "Canada",
            isoCode = "CA",
            flagEmoji = "🇨🇦",
            dialCode = "+1",
            defaultCurrency = "USD",
            regionLabel = "Province",
            subRegionLabel = "City / Municipality",
            regions = linkedMapOf(
                "Ontario" to listOf("Toronto", "Ottawa", "Mississauga", "Brampton", "Hamilton"),
                "Quebec" to listOf("Montreal", "Quebec City", "Laval"),
                "British Columbia" to listOf("Vancouver", "Surrey", "Victoria", "Burnaby"),
                "Alberta" to listOf("Calgary", "Edmonton")
            )
        ),
        CountryLocationSpec(
            name = "Australia",
            isoCode = "AU",
            flagEmoji = "🇦🇺",
            dialCode = "+61",
            defaultCurrency = "USD",
            regionLabel = "State / Territory",
            subRegionLabel = "City / Suburb",
            regions = linkedMapOf(
                "New South Wales" to listOf("Sydney", "Parramatta", "Lakemba", "Newcastle"),
                "Victoria" to listOf("Melbourne", "Dandenong", "Geelong"),
                "Queensland" to listOf("Brisbane", "Gold Coast", "Sunshine Coast"),
                "Western Australia" to listOf("Perth", "Fremantle")
            )
        ),
        CountryLocationSpec(
            name = "Qatar",
            isoCode = "QA",
            flagEmoji = "🇶🇦",
            dialCode = "+974",
            defaultCurrency = "USD",
            regionLabel = "Municipality",
            subRegionLabel = "Zone / City",
            regions = linkedMapOf(
                "Doha" to listOf("Doha City", "West Bay", "Al Sadd", "Najma"),
                "Al Wakrah" to listOf("Al Wakrah City", "Mesaieed"),
                "Al Rayyan" to listOf("Al Rayyan", "Education City")
            )
        ),
        CountryLocationSpec(
            name = "Kuwait",
            isoCode = "KW",
            flagEmoji = "🇰🇼",
            dialCode = "+965",
            defaultCurrency = "USD",
            regionLabel = "Governorate",
            subRegionLabel = "Area",
            regions = linkedMapOf(
                "Al Asimah" to listOf("Kuwait City", "Sharq", "Mirqab"),
                "Hawalli" to listOf("Salmiya", "Hawalli", "Jabriya"),
                "Farwaniya" to listOf("Farwaniya", "Jleeb Al-Shuyoukh")
            )
        ),
        CountryLocationSpec(
            name = "Oman",
            isoCode = "OM",
            flagEmoji = "🇴🇲",
            dialCode = "+968",
            defaultCurrency = "USD",
            regionLabel = "Governorate",
            subRegionLabel = "Wilayat",
            regions = linkedMapOf(
                "Muscat" to listOf("Muscat", "Muttrah", "Ruwi", "Seeb", "Bawshar"),
                "Dhofar" to listOf("Salalah", "Taqah"),
                "Al Batinah North" to listOf("Sohar", "Saham")
            )
        ),
        CountryLocationSpec(
            name = "Germany",
            isoCode = "DE",
            flagEmoji = "🇩🇪",
            dialCode = "+49",
            defaultCurrency = "EUR",
            regionLabel = "Federal State",
            subRegionLabel = "City / District",
            regions = linkedMapOf(
                "Berlin" to listOf("Mitte", "Kreuzberg", "Charlottenburg", "Neukolln"),
                "Bavaria" to listOf("Munich", "Nuremberg", "Augsburg"),
                "North Rhine-Westphalia" to listOf("Cologne", "Dusseldorf", "Dortmund"),
                "Hamburg" to listOf("Hamburg-Mitte", "Altona")
            )
        ),
        CountryLocationSpec(
            name = "Japan",
            isoCode = "JP",
            flagEmoji = "🇯🇵",
            dialCode = "+81",
            defaultCurrency = "USD",
            regionLabel = "Prefecture",
            subRegionLabel = "Ward / City",
            regions = linkedMapOf(
                "Tokyo" to listOf("Shinjuku", "Shibuya", "Minato", "Toshima", "Kita"),
                "Osaka" to listOf("Osaka City", "Sakai", "Suita"),
                "Kanagawa" to listOf("Yokohama", "Kawasaki")
            )
        )
    )

    fun findCountry(nameOrIso: String): CountryLocationSpec {
        val clean = nameOrIso.trim()
        return countries.firstOrNull {
            it.name.equals(clean, ignoreCase = true) ||
                it.isoCode.equals(clean, ignoreCase = true)
        } ?: countries.first()
    }

    fun countryFor(nameOrIso: String): CountryLocationSpec = findCountry(nameOrIso)

    fun flagForCountry(countryName: String): String {
        val match = countries.firstOrNull {
            it.name.equals(countryName.trim(), ignoreCase = true) ||
                it.isoCode.equals(countryName.trim(), ignoreCase = true)
        }
        return match?.flagEmoji ?: "🌍"
    }

    fun districtsFor(countryName: String): List<String> {
        return findCountry(countryName).regions.keys.toList()
    }

    fun regionsForCountry(countryName: String): List<String> = districtsFor(countryName)

    fun upazilasFor(countryName: String, district: String): List<String> {
        val spec = findCountry(countryName)
        return spec.regions.entries.firstOrNull {
            it.key.equals(district.trim(), ignoreCase = true)
        }?.value ?: spec.regions.values.firstOrNull().orEmpty()
    }

    fun subRegionsFor(countryName: String, district: String): List<String> = upazilasFor(countryName, district)
}
