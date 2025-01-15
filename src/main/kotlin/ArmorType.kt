import com.github.knokko.bitser.BitEnum
import com.github.knokko.bitser.BitStruct
import com.github.knokko.bitser.field.BitField

@BitEnum(mode = BitEnum.Mode.UniformOrdinal)
enum class EquipmentSlotType {
	Body,
	Head,
	Shield,
	Accessory
}

@BitStruct(backwardCompatible = false)
class ArmorType(

	@BitField(ordering = 0)
	var key: String,

	@BitField(ordering = 1)
	var name: String,

	@BitField(ordering = 2)
	var slotType: EquipmentSlotType
) {
	@Suppress("unused")
	private constructor() : this("", "", EquipmentSlotType.Accessory)

	override fun toString() = "ArmorType($key, $name)"
}

@BitStruct(backwardCompatible = false)
class Root {

	@BitField(ordering = 0)
	val armorTypes = ArrayList<ArmorType>()

	override fun toString() = armorTypes.toString()
}
