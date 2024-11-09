package ChatBOt;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.*;

public class ChatBot extends javax.swing.JFrame {

    private int chatCount = 1, chatSelected = 0;
    private final ArrayList<JButton> chatButtons = new ArrayList<>();
    private ArrayList<String> chatMsgs = new ArrayList<>();

    public ChatBot() {
        initComponents();

        getContentPane().setBackground(new java.awt.Color(33, 33, 33));

        setTitle("Ollama Chatbot");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        respuestaField.setLineWrap(true);
        respuestaField.setWrapStyleWord(true);
        respuestaField.setEditable(false);
        respuestaField.setLineWrap(true);
        chatMsgs.add("");

        Dimension buttonSize = new Dimension(120, 40);
        jButton1.setMinimumSize(buttonSize);
        jButton1.setPreferredSize(buttonSize);
        jButton1.setMaximumSize(buttonSize);
        jButton1.setAlignmentX(Component.LEFT_ALIGNMENT);

        jButton1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                chatSelected = 0; // El índice del primer chat es 0
                actualizarHistorial();
            }
        });
    }

    private String obtenerRespuestaDeOllama(String mensaje) {
        final String mensajeDemora = "Cargando respuesta del chatbot, por favor espera...";
        final int maxIntentos = 3;
        int intentos = 0;

        Timer timer = new Timer(3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                respuestaField.append("Bot: " + mensajeDemora + "\n");
                chatMsgs.set(chatSelected, chatMsgs.get(chatSelected) + "Bot: " + mensajeDemora + "\n");
            }
        });
        timer.setRepeats(false);
        timer.start();

        while (intentos < maxIntentos) {
            try {
                String urlString = "http://localhost:11434/api/chat";
                String jsonInputString = "{\"model\": \"llama3.2:1b\", \"stream\": false, \"messages\": [ { \"role\": \"user\", \"content\": \" Responde en español: " + mensaje + " \" } ] }"; // Solicitud

                URL url = new URL(urlString);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json; utf-8");
                con.setRequestProperty("Accept", "application/json");
                con.setDoOutput(true);

                try ( OutputStream os = con.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
                String inputLine;
                StringBuilder respuesta = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    respuesta.append(inputLine);
                }
                in.close();
                timer.stop();
                return capturarRespuesta(respuesta.toString());
                
            } catch (java.net.ConnectException e) {
                intentos++;
                if (intentos >= maxIntentos) {
                    timer.stop();
                    respuestaField.append("Bot: No se pudo conectar al servidor después de varios intentos.\n");
                    chatMsgs.set(chatSelected, chatMsgs.get(chatSelected) + "Bot: No se pudo conectar al servidor después de varios intentos.\n");
                    return "Error de conexión. Verifica la red o el servidor de la API.";
                }
            } catch (java.io.IOException e) {
                intentos++;
                if (intentos >= maxIntentos) {
                    timer.stop();
                    respuestaField.append("Bot: Error de entrada/salida en la conexión con la API.\n");
                    chatMsgs.set(chatSelected, chatMsgs.get(chatSelected) + "Bot: Error de entrada/salida en la conexión con la API.\n");
                    return "Error al conectar con la API. Inténtalo más tarde.";
                }
            } catch (Exception e) {
                System.out.println(e);
                timer.stop();
                respuestaField.append("Bot: " + mensajeDemora + "\n");
                chatMsgs.set(chatSelected, chatMsgs.get(chatSelected) + "Bot: " + mensajeDemora + "\n");
                return "Error al conectar con el chatbot.";
            }
        }
        return "Error desconocido. Intente denuevo.";
    }

    private String capturarRespuesta(String jsonResponse) {
        try {
            System.out.println(jsonResponse);
            String searchKey = "\"content\":\"";
            int startIndex = jsonResponse.indexOf(searchKey) + searchKey.length();
            int endIndex = jsonResponse.indexOf("\"},\"done_reason", startIndex);
            return jsonResponse.substring(startIndex, endIndex);

        } catch (Exception e) {
            System.out.println(e);
            return "Error al procesar la respuesta del chatbot.";
        }
    }

    private void actualizarHistorial() {
        respuestaField.setText(chatMsgs.get(chatSelected));
    }

    private void enviarPregunta() {
        String userText = mensajeField.getText();
        respuestaField.append("Tú: " + userText + "\n");
        chatMsgs.set(chatSelected, chatMsgs.get(chatSelected) + "Tú: " + userText + "\n");

        mensajeField.setText("");

        String botResponse = obtenerRespuestaDeOllama(userText); // Subrutina de API
        respuestaField.append("Bot: " + botResponse + "\n");
        chatMsgs.set(chatSelected, chatMsgs.get(chatSelected) + "Bot: " + botResponse + "\n");
    }

    private void nuevoChat() {
        JButton nuevoChatButton = new JButton("Chat " + (chatCount + 1));
        chatMsgs.add("");
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        final int newChatIndex = chatCount;

        nuevoChatButton.setFont(new java.awt.Font("Yu Gothic UI Semibold", 1, 16));
        nuevoChatButton.setFocusPainted(false);
        nuevoChatButton.setOpaque(true);
        nuevoChatButton.setBackground(new java.awt.Color(33, 33, 33));
        nuevoChatButton.setForeground(new java.awt.Color(255, 255, 255));

        Dimension buttonSize = jButton1.getPreferredSize();
        nuevoChatButton.setMinimumSize(buttonSize);
        nuevoChatButton.setPreferredSize(buttonSize);
        nuevoChatButton.setMaximumSize(buttonSize);

        nuevoChatButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        nuevoChatButton.setPreferredSize(jButton1.getPreferredSize());

        nuevoChatButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                chatSelected = newChatIndex;
                respuestaField.setText(chatMsgs.get(chatSelected));
            }
        });
        chatCount++;

        chatButtons.add(nuevoChatButton);
        panel.add(nuevoChatButton, panel.getComponentCount());
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        panel.revalidate();
        panel.repaint();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        respuestaField = new javax.swing.JTextArea();
        mensajeField = new javax.swing.JTextField();
        enviarButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        panel = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        nuevoButton = new javax.swing.JButton();
        vaciarButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(33, 33, 33));

        respuestaField.setBackground(new java.awt.Color(47, 47, 47));
        respuestaField.setColumns(20);
        respuestaField.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 18)); // NOI18N
        respuestaField.setForeground(new java.awt.Color(255, 255, 255));
        respuestaField.setRows(5);
        respuestaField.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(103, 103, 103), 5, true));
        respuestaField.setSelectionColor(new java.awt.Color(255, 255, 255));
        jScrollPane1.setViewportView(respuestaField);

        mensajeField.setBackground(new java.awt.Color(47, 47, 47));
        mensajeField.setFont(new java.awt.Font("Yu Gothic UI Semibold", 1, 18)); // NOI18N
        mensajeField.setForeground(new java.awt.Color(255, 255, 255));
        mensajeField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        mensajeField.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(103, 103, 103), 5));
        mensajeField.setDisabledTextColor(new java.awt.Color(47, 47, 47));
        mensajeField.setMargin(new java.awt.Insets(15, 15, 55, 20));
        mensajeField.setSelectedTextColor(new java.awt.Color(47, 47, 47));
        mensajeField.setSelectionColor(new java.awt.Color(255, 255, 255));

        enviarButton.setBackground(new java.awt.Color(33, 33, 33));
        enviarButton.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 18)); // NOI18N
        enviarButton.setForeground(new java.awt.Color(255, 255, 255));
        enviarButton.setText("Enviar");
        enviarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enviarButtonActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Yu Gothic UI Semibold", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Chats");

        jScrollPane2.setBorder(null);

        panel.setBackground(new java.awt.Color(33, 33, 33));
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));

        jButton1.setBackground(new java.awt.Color(33, 33, 33));
        jButton1.setFont(new java.awt.Font("Yu Gothic UI Semibold", 1, 18)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Chat 1");
        jButton1.setMaximumSize(new java.awt.Dimension(120, 40));
        jButton1.setMinimumSize(new java.awt.Dimension(120, 40));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        panel.add(jButton1);

        jScrollPane2.setViewportView(panel);

        nuevoButton.setBackground(new java.awt.Color(33, 33, 33));
        nuevoButton.setFont(new java.awt.Font("Yu Gothic UI Semibold", 1, 18)); // NOI18N
        nuevoButton.setForeground(new java.awt.Color(255, 255, 255));
        nuevoButton.setText("Nuevo chat");
        nuevoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nuevoButtonActionPerformed(evt);
            }
        });

        vaciarButton.setBackground(new java.awt.Color(33, 33, 33));
        vaciarButton.setFont(new java.awt.Font("Yu Gothic UI Semibold", 1, 18)); // NOI18N
        vaciarButton.setForeground(new java.awt.Color(255, 255, 255));
        vaciarButton.setText("Vaciar chat");
        vaciarButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                vaciarButtonMouseClicked(evt);
            }
        });
        vaciarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vaciarButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(94, 94, 94)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE)
                    .addComponent(mensajeField))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(enviarButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(nuevoButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(vaciarButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(60, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 243, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nuevoButton))
                    .addComponent(jScrollPane1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(vaciarButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(enviarButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap(39, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(mensajeField, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void enviarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enviarButtonActionPerformed
        enviarPregunta();
    }//GEN-LAST:event_enviarButtonActionPerformed

    private void nuevoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nuevoButtonActionPerformed
        nuevoChat();
    }//GEN-LAST:event_nuevoButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    }//GEN-LAST:event_jButton1ActionPerformed

    private void vaciarButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_vaciarButtonMouseClicked

    }//GEN-LAST:event_vaciarButtonMouseClicked

    private void vaciarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vaciarButtonActionPerformed
        if (chatMsgs.get(chatSelected).isEmpty()) {
            JOptionPane.showMessageDialog(null, "El chat ya está vacío.");
        } else {
            int respuesta = JOptionPane.showConfirmDialog(null,
                    "¿Estás seguro de que deseas eliminar este elemento?",
                    "Confirmación",
                    JOptionPane.YES_NO_OPTION);
            if (respuesta == JOptionPane.YES_OPTION) {
                respuestaField.setText("");
                chatMsgs.set(chatSelected, "");
                JOptionPane.showMessageDialog(null, "Elemento eliminado correctamente.");
            } else {
                JOptionPane.showMessageDialog(null, "Acción cancelada.");
            }
        }
    }//GEN-LAST:event_vaciarButtonActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ChatBot().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton enviarButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField mensajeField;
    private javax.swing.JButton nuevoButton;
    private javax.swing.JPanel panel;
    private javax.swing.JTextArea respuestaField;
    private javax.swing.JButton vaciarButton;
    // End of variables declaration//GEN-END:variables
}