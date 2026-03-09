import { View, Text, StyleSheet } from 'react-native';
import { Theme } from '@/constants/Colors';
import { Bell } from 'lucide-react-native';

export default function NotificationsScreen() {
    return (
        <View style={styles.container}>
            <View style={styles.header}>
                <Text style={styles.title}>Notifications</Text>
            </View>

            <View style={styles.content}>
                <Bell color={Theme.colors.secondary} size={64} style={{ marginBottom: 20 }} />
                <Text style={styles.emptyText}>No new notifications</Text>
            </View>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: Theme.colors.background,
    },
    header: {
        paddingTop: 60,
        paddingHorizontal: 20,
        paddingBottom: 20,
        borderBottomWidth: 1,
        borderBottomColor: Theme.colors.border,
    },
    title: {
        fontSize: 32,
        fontWeight: 'bold',
        color: Theme.colors.text,
    },
    content: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        padding: 40,
    },
    emptyText: {
        fontSize: 18,
        color: Theme.colors.secondary,
        textAlign: 'center',
    },
});
