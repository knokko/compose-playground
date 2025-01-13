enum class EquipmentSlotType {
	Body,
	Head,
	Shield,
	Accessory
}

class ArmorType(
	var key: String,
	var name: String,
	var slotType: EquipmentSlotType
) {
	override fun toString() = "ArmorType($key, $name)"
}
