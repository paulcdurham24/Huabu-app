import * as functions from "firebase-functions/v2";
import * as admin from "firebase-admin";

admin.initializeApp();

const db = admin.firestore();
const messaging = admin.messaging();

/**
 * Triggered when a new notification document is created in Firestore.
 * Looks up the recipient user's FCM token and sends a push notification.
 */
export const sendPushOnNewNotification = functions.firestore.onDocumentCreated(
  "notifications/{notifId}",
  async (event) => {
    const data = event.data?.data();
    if (!data) return;

    const {userId, title, message, type, senderId} = data as {
      userId: string;
      title: string;
      message: string;
      type: string;
      senderId: string;
    };

    if (!userId || !title) return;

    // Fetch recipient's FCM token
    const userDoc = await db.collection("users").doc(userId).get();
    const fcmToken = userDoc.data()?.fcmToken as string | undefined;

    if (!fcmToken) {
      functions.logger.info(`No FCM token for user ${userId}`);
      return;
    }

    const notifId = event.params.notifId;

    const notificationPayload: admin.messaging.Message = {
      token: fcmToken,
      notification: {
        title,
        body: message,
      },
      android: {
        priority: "high",
        notification: {
          channelId: "huabu_default",
          color: "#FF1B6B",
          clickAction: "OPEN_APP",
        },
      },
      data: {
        type: type ?? "",
        senderId: senderId ?? "",
        notifId,
        click_action: "OPEN_APP",
      },
    };

    try {
      const response = await messaging.send(notificationPayload);
      functions.logger.info(`Sent FCM push to ${userId}: ${response}`);
    } catch (err) {
      functions.logger.error(`Failed to send FCM push to ${userId}`, err);
    }
  }
);

/**
 * Triggered when a new message is sent in a conversation.
 * Sends a push notification to the recipient.
 */
export const sendPushOnNewMessage = functions.firestore.onDocumentCreated(
  "conversations/{convId}/messages/{msgId}",
  async (event) => {
    const data = event.data?.data();
    if (!data) return;

    const {senderId, senderName, content, conversationId} = data as {
      senderId: string;
      senderName: string;
      content: string;
      conversationId: string;
    };

    const convId = event.params.convId;

    // Get conversation participants
    const convDoc = await db.collection("conversations").doc(convId).get();
    const participants = convDoc.data()?.participants as string[] | undefined;
    if (!participants) return;

    // Notify everyone except the sender
    const recipients = participants.filter((uid) => uid !== senderId);

    await Promise.all(
      recipients.map(async (recipientId) => {
        const userDoc = await db.collection("users").doc(recipientId).get();
        const fcmToken = userDoc.data()?.fcmToken as string | undefined;
        if (!fcmToken) return;

        const msg: admin.messaging.Message = {
          token: fcmToken,
          notification: {
            title: senderName,
            body: content.length > 80 ? content.substring(0, 80) + "…" : content,
          },
          android: {
            priority: "high",
            notification: {
              channelId: "huabu_messages",
              color: "#FF1B6B",
            },
          },
          data: {
            type: "message",
            senderId,
            conversationId: conversationId ?? convId,
            click_action: "OPEN_APP",
          },
        };

        try {
          await messaging.send(msg);
        } catch (err) {
          functions.logger.error(`Failed to push message to ${recipientId}`, err);
        }
      })
    );
  }
);
