package es.bdo.skeleton.shared.request.converter

import es.bdo.skeleton.shared.model.FilterGroup
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import tools.jackson.core.exc.StreamReadException
import tools.jackson.databind.DatabindException
import tools.jackson.databind.ObjectMapper

@Component
class ArrayFilterGroupConverter(private val objectMapper: ObjectMapper) : Converter<String, Array<FilterGroup>> {
    override fun convert(value: String): Array<FilterGroup> {
        try {
            return objectMapper.readValue(value, Array<FilterGroup>::class.java)
        } catch (_: StreamReadException) {
            // ignore
        } catch (_: DatabindException) {
            // ignore
        }

        return arrayOf()
    }
}
