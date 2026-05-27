package com.grankain.platformapi.auth.exceptions;

/**
 * Exceção de domínio para representar que o endereço IP do usuário está temporariamente bloqueado.
 * Segue as melhores práticas de Clean Architecture e DDD ao encapsular erros de negócio específicos.
 */
public class BlockedIpException extends RuntimeException {
    
    public BlockedIpException(String message) {
        super(message);
    }
}
