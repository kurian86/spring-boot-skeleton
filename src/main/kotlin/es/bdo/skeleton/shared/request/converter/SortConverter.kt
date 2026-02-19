package es.bdo.skeleton.shared.request.converter

import es.bdo.skeleton.shared.model.Sort
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import tools.jackson.core.exc.StreamReadException
import tools.jackson.databind.DatabindException
import tools.jackson.databind.ObjectMapper

@Component
class SortConverter(private val objectMapper: ObjectMapper) : Converter<String, Sort?> {
    override fun convert(value: String): Sort? {
        try {
            return objectMapper.readValue(value, Sort::class.java)
        } catch (_: StreamReadException) {
            // ignore
        } catch (_: DatabindException) {
            // ignore
        }

        return null
    }
}
