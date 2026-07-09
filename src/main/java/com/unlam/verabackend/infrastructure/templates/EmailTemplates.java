package com.unlam.verabackend.infrastructure.templates;

public final class EmailTemplates {

    private EmailTemplates() {}

    public static String passwordReset(String resetLink) {
        return """
            <!DOCTYPE html>
            <html>
            <body style="margin:0;padding:0;background-color:#05070D;font-family:Arial,sans-serif;">
            <table width="100%%" cellpadding="0" cellspacing="0">
                <tr><td align="center" style="padding:40px 20px;">
                    <table width="600" cellpadding="0" cellspacing="0" style="background:#0B0D17;border-radius:20px;overflow:hidden;border:1px solid #1A1D29;">
                        <tr><td align="center" style="padding:50px 40px 20px 40px;">
                            <img src="https://vera-app.vercel.app/Isologo_Vera.png" alt="VERA" width="180"/>
                        </td></tr>
                        <tr><td align="center" style="padding:10px 40px;">
                            <h1 style="color:white;margin:0;font-size:34px;">Recuperación de contraseña</h1>
                        </td></tr>
                        <tr><td align="center" style="padding:20px 50px;">
                            <p style="color:#B7BDC9;font-size:16px;line-height:28px;margin:0;">
                                Recibimos una solicitud para restablecer la contraseña de tu cuenta.
                            </p>
                        </td></tr>
                        <tr><td align="center" style="padding:20px;">
                            <a href="%s" style="background:#0D6EFD;color:white;text-decoration:none;padding:16px 32px;border-radius:12px;display:inline-block;font-size:16px;font-weight:bold;">
                                Restablecer contraseña
                            </a>
                        </td></tr>
                        <tr><td align="center" style="padding:10px 50px 30px 50px;">
                            <p style="color:#8A91A1;font-size:14px;line-height:24px;">
                                Si el botón no funciona, copiá y pegá este enlace en tu navegador:
                            </p>
                            <a href="%s" style="color:#4D9DFF;word-break:break-all;">%s</a>
                        </td></tr>
                        <tr><td align="center" style="padding:20px 50px;border-top:1px solid #1A1D29;">
                            <p style="color:#B7BDC9;font-size:14px;margin:0;">Este enlace expirará en 1 hora.</p>
                            <p style="color:#8A91A1;font-size:13px;margin-top:20px;">Si no realizaste esta solicitud, podés ignorar este correo.</p>
                            <p style="color:white;margin-top:30px;font-weight:bold;">Equipo VERA</p>
                        </td></tr>
                    </table>
                </td></tr>
            </table>
            </body></html>
            """.formatted(resetLink, resetLink, resetLink);
    }

    public static String emailVerification(String verificationLink) {
        return """
            <!DOCTYPE html>
            <html>
            <body style="margin:0;padding:0;background-color:#05070D;font-family:Arial,sans-serif;">
            <table width="100%%" cellpadding="0" cellspacing="0">
                <tr><td align="center" style="padding:40px 20px;">
                    <table width="600" cellpadding="0" cellspacing="0" style="background:#0B0D17;border-radius:20px;overflow:hidden;border:1px solid #1A1D29;">
                        <tr><td align="center" style="padding:50px 40px 20px 40px;">
                            <img src="https://vera-app.vercel.app/Isologo_Vera.png" alt="VERA" width="180"/>
                        </td></tr>
                        <tr><td align="center" style="padding:10px 40px;">
                            <h1 style="color:white;margin:0;font-size:30px;">¡Te damos la bienvenida a VERA!</h1>
                        </td></tr>
                        <tr><td align="center" style="padding:20px 50px;">
                            <p style="color:#B7BDC9;font-size:16px;line-height:28px;margin:0;">
                                Estás a un solo paso de activar tu cuenta y empezar a proteger a las personas que más querés contra estafas digitales.
                            </p>
                        </td></tr>
                        <tr><td align="center" style="padding:20px;">
                            <a href="%s" style="background:#0D6EFD;color:white;text-decoration:none;padding:16px 32px;border-radius:12px;display:inline-block;font-size:16px;font-weight:bold;">
                                Verificar mi Correo
                            </a>
                        </td></tr>
                        <tr><td align="center" style="padding:10px 50px 30px 50px;border-top:1px solid #1A1D29;">
                            <p style="color:#8A91A1;font-size:14px;line-height:24px;margin:0;">
                                Si el botón no funciona, copiá y pegá este enlace en tu navegador:
                            </p>
                            <a href="%s" style="color:#4D9DFF;word-break:break-all;">%s</a>
                            <p style="color:#8A91A1;font-size:13px;margin-top:20px;">Este enlace expirará en 24 horas.</p>
                            <p style="color:white;margin-top:30px;font-weight:bold;">Equipo VERA</p>
                        </td></tr>
                    </table>
                </td></tr>
            </table>
            </body></html>
            """.formatted(verificationLink, verificationLink, verificationLink);
    }
}