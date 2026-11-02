# BSB Food — App Android

App Android (Capacitor) que abre o **www.bsbfoods.com.br** dentro do aplicativo. Todas as funções do site (cliente, lojista e entregador) funcionam no app e acompanham as atualizações do site, sem precisar gerar um novo APK.

## O que o app já faz

- Abre o BSB Food em tela cheia, com ícone, splash laranja e barra de status na cor da marca
- Tela própria "Sem conexão" com botão de tentar novamente (volta sozinho quando a internet voltar)
- Localização (GPS) para o endereço do cliente e para o entregador
- Câmera / galeria para o lojista enviar fotos de produtos
- Links do WhatsApp e de pagamento abrem normalmente
- Botão voltar do Android navega pelo site

## Como gerar o APK (pelo GitHub, sem computador)

1. Crie um repositório novo no GitHub (ex.: `bsb-food-app`)
2. Envie **todos** os arquivos deste projeto, incluindo a pasta `.github/workflows`
3. Abra a aba **Actions**, escolha **Gerar APK BSB Food** e toque em **Run workflow** (também roda sozinho a cada envio na branch `main`)
4. Quando terminar (uns 5 a 8 minutos), abra a execução e baixe **BSB-Food-apk** na seção *Artifacts*
5. Extraia o ZIP e instale o `app-debug.apk` no celular (permita "instalar apps desconhecidos")

## Para mudar o endereço do site

Edite `server.url` em `capacitor.config.json`.

## Limitações conhecidas

- O APK gerado é de teste (debug). Para publicar na Play Store é preciso assinar um APK/AAB de release.
- Notificações push nativas e rastreamento do entregador com o app fechado exigem Firebase e um serviço em segundo plano; não estão incluídos nesta primeira versão.
- Se o site usa Mercado Pago, PagSeguro ou outro gateway diferente dos listados em `allowNavigation`, adicione o domínio dele na lista.
