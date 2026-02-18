package es.bdo.skeleton.shared.extension

import com.fasterxml.uuid.Generators
import java.util.*

fun newUUID(): UUID {
    return Generators.timeBasedGenerator().generate()
}
