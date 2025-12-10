package fr.cat.tcc.sj.adapter.api_message_exchange;

import fr.cat.tcc.commons.back.api.handlers.RequestHandler;
import fr.cat.tcc.commons.back.model.ResponseApiError;
import fr.cat.tcc.sj.adapter.api_message_exchange.dto.MessageDto;
import fr.cat.tcc.sj.adapter.api_message_exchange.dto.MessageExchangeApiResponse;
import fr.cat.tcc.sj.adapter.api_message_exchange.exception.ApiMessageExchangeException;
import fr.cat.tcc.sj.adapter.api_message_exchange.mapper.MessageExchangeApiDtoMessageMapper;
import fr.cat.tcc.sj.application.message_exchange.dto.MessageExchangeApiDto;
import fr.cat.tcc.sj.application.message_exchange.port.out.ApiMessageExchangePort;
import fr.cat.tcc.sj.application.swift.dto.SwiftDto;
import fr.cat.tcc.sj.application.swift.service.MxCrudService;
import fr.cat.tcc.sj.configs.PhenicsAPIConfigurationProperties;
import fr.cat.tcc.sj.domain.shared.exception.TechnicalException;
import io.vavr.control.Either;
import io.vavr.control.Try;    
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiMessageExchangeAdapter implements ApiMessageExchangePort {

    public static final String GET_MESSAGE_EXCHANGE_ENDPOINT = "/raw_messages/v1/exchanges/%s/messages";
    private final PhenicsAPIConfigurationProperties phenicsAPIConfigurationProperties;
    private final RequestHandler requestHandler;
    private final MessageExchangeApiDtoMessageMapper messageExchangeApiDtoMessageMapper;
    private final MxCrudService mxCrudService;

    @Override
    public Either<TechnicalException, MessageExchangeApiDto> getMessageExchangeByOperationId(String operationId) {
        Optional<SwiftDto> swiftDtoOptional = mxCrudService.getSwiftDtoByOperationId(operationId);
        if (swiftDtoOptional.isEmpty()) {
            return Either.left(new TechnicalException("TechnicalException SwiftEntity not found for operationId=" + operationId));
        }
        String correlationId = swiftDtoOptional.get().correlationId();
        Try<Either<ResponseApiError, MessageExchangeApiResponse>> messageExchangeApiResponseTry = Try.of(() -> getMessageExchangeFromExternalApi(correlationId));
        if (messageExchangeApiResponseTry.isFailure()) {
            return Either.left(new TechnicalException("TechnicalException " + messageExchangeApiResponseTry.getCause().getMessage()));
        }
        Either<ResponseApiError, MessageExchangeApiResponse> response = messageExchangeApiResponseTry.get();

        if (response.isLeft()) {
            ResponseApiError apiError = response.getLeft();
            if (apiError.getStatus() == HttpStatus.NOT_FOUND) {
                return Either.right(null);
            }
            if (!ObjectUtils.isEmpty(apiError.getErrors())
                    && !ObjectUtils.isEmpty(apiError.getErrors().get(0).getLabel())) {
                return Either.left(new ApiMessageExchangeException(apiError.getStatus(), apiError.getErrors().get(0).getLabel(), correlationId));
            } else {
                return Either.left(new ApiMessageExchangeException(apiError.getStatus(), "Unknown error occurred", correlationId));
            }
        }

        List<MessageDto> messages = response.get().messages();
        if (messages.size() > 1) {
            log.warn("Api Exchange returns more than one message for operation id={}", operationId);
        }

        MessageExchangeApiDto messagesExchangeApiDto = messageExchangeApiDtoMessageMapper.toLeft(messages.get(0));

        //Map operationId
        MessageExchangeApiDto.MessageExchangeApiDtoBuilder messageExchangeApiDtoBuilder = messagesExchangeApiDto.toBuilder();
        messageExchangeApiDtoBuilder.operationId(operationId);

        return Either.right(messageExchangeApiDtoBuilder.build());
    }

    private Either<ResponseApiError, MessageExchangeApiResponse> getMessageExchangeFromExternalApi(@Nonnull final String exchangeId) {
        final String requestUrl = phenicsAPIConfigurationProperties.getPrefix() + String.format(GET_MESSAGE_EXCHANGE_ENDPOINT, exchangeId);
        return requestHandler.get(requestUrl, MessageExchangeApiResponse.class);

    }
}
